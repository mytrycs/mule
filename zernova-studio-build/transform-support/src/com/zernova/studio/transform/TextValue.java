/* Copyright (c) 2026 ZerNova. Materialize bounded textual expression results. */
package com.zernova.studio.transform;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.zernova.runtime.api.streaming.CursorProvider;
final class TextValue {
 static String read(Object value){
  if(value instanceof String text)return text;
  if(value instanceof byte[] bytes)return new String(bytes,StandardCharsets.UTF_8);
  try{
   if(value instanceof CursorProvider<?> provider){try(var cursor=provider.openCursor()){if(cursor instanceof InputStream stream)return readStream(stream);throw new IllegalArgumentException("Expected a text result, not an object cursor.");}}
   if(value instanceof InputStream stream)return readStream(stream);
   return String.valueOf(value);
  }catch(IOException error){throw new IllegalStateException("Cannot read text expression result.",error);}
 }
 private static String readStream(InputStream stream)throws IOException{byte[] bytes=stream.readNBytes(1048577);if(bytes.length>1048576)throw new IllegalArgumentException("Text expression result exceeds 1 MiB.");return new String(bytes,StandardCharsets.UTF_8);}
}
