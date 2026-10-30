/* Copyright (c) 2026 ZerNova. Original runtime lifecycle infrastructure.
 * Development use only. Inherited engine files retain their own licenses.
 * This agent neither implements nor interprets integration applications.
 */
package io.zernova.studio;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RuntimeControlAgent {
  private static FileChannel lockChannel;
  private static FileLock lock;
  private static ServerSocket server;
  private static final AtomicBoolean stopping = new AtomicBoolean();
  private RuntimeControlAgent() {}
  public static void premain(String arguments, Instrumentation ignored) throws Exception {
    if (arguments == null || arguments.isBlank()) throw new IllegalArgumentException("Missing ZERNOVA control directory");
    Path control = Path.of(arguments).toAbsolutePath().normalize();
    if (Files.isSymbolicLink(control) || !Files.isDirectory(control, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Control directory must be an ordinary private directory");
    String token = Files.readString(control.resolve("auth.token"), StandardCharsets.US_ASCII).trim();
    String runId = Files.readString(control.resolve("run-id.txt"), StandardCharsets.US_ASCII).trim();
    if (!token.matches("[a-f0-9]{64}") || !runId.matches("[a-f0-9-]{36}")) throw new IOException("Invalid ZERNOVA control identity");
    lockChannel = FileChannel.open(control.resolve("instance.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    lock = lockChannel.tryLock();
    if (lock == null) throw new IOException("Another runtime owns this instance");
    server = new ServerSocket();
    server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0));
    Path state = control.resolve("endpoint.json");
    String identity = "\"schemaVersion\":1,\"product\":\"ZERNOVA Runtime Control\",\"pid\":" + ProcessHandle.current().pid() + ",\"runId\":\"" + runId + "\",\"port\":" + server.getLocalPort();
    Path tmp = control.resolve("endpoint." + runId + ".tmp");
    Files.writeString(tmp, "{" + identity + "}\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    privateFile(tmp);
    try { Files.move(tmp, state, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
    catch (AtomicMoveNotSupportedException e) { Files.move(tmp, state, StandardCopyOption.REPLACE_EXISTING); }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try { server.close(); } catch (IOException ignoredClose) {}
      try { Files.deleteIfExists(state); } catch (IOException ignoredDelete) {}
      try { lock.release(); lockChannel.close(); } catch (IOException ignoredLock) {}
    }, "zernova-control-cleanup"));
    Thread listener = new Thread(() -> accept(token, identity), "zernova-local-control"); listener.setDaemon(true); listener.start();
  }
  private static void privateFile(Path file) throws IOException {
    try { Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------")); }
    catch (UnsupportedOperationException ignored) { }
  }
  private static void accept(String token, String identity) {
    byte[] expected = token.getBytes(StandardCharsets.US_ASCII);
    while (!server.isClosed()) {
      try (Socket socket = server.accept()) {
        if (!socket.getInetAddress().isLoopbackAddress()) continue;
        socket.setSoTimeout(1500);
        InputStream in = socket.getInputStream(); ByteArrayOutputStream bytes = new ByteArrayOutputStream(); boolean complete = false;
        for (int count = 0; count < 96; count++) { int value = in.read(); if (value < 0) break; if (value == '\n') { complete = true; break; } bytes.write(value); }
        if (!complete) continue;
        String[] parts = bytes.toString(StandardCharsets.US_ASCII).split(" ", -1);
        if (parts.length != 2 || !MessageDigest.isEqual(expected, parts[1].getBytes(StandardCharsets.US_ASCII))) { socket.getOutputStream().write("{\"error\":\"Unauthorized\"}\n".getBytes(StandardCharsets.US_ASCII)); continue; }
        if (!parts[0].equals("STATUS") && !parts[0].equals("STOP")) { socket.getOutputStream().write("{\"error\":\"Unsupported command\"}\n".getBytes(StandardCharsets.US_ASCII)); continue; }
        boolean stop = parts[0].equals("STOP");
        socket.getOutputStream().write(("{" + identity + ",\"stopping\":" + (stop || stopping.get()) + "}\n").getBytes(StandardCharsets.UTF_8)); socket.getOutputStream().flush();
        if (stop && stopping.compareAndSet(false, true)) { Thread shutdown = new Thread(() -> System.exit(0), "zernova-owner-requested-stop"); shutdown.setDaemon(false); shutdown.start(); }
      } catch (IOException failure) { if (!server.isClosed()) System.err.println("ZERNOVA local control: " + failure.getClass().getSimpleName()); }
    }
  }
}
