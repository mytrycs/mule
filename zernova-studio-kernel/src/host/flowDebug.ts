/* Copyright (c) 2026 ZerNova. JDWP flow debugging for the bundled engine. */
import net from 'node:net';
export interface FlowStop {
    path: string;
    thread: string;
}
class Reader {
    offset = 0;
    constructor(readonly b: Buffer) { }
    byte() { return this.b.readUInt8(this.offset++); }
    int() { const n = this.b.readInt32BE(this.offset); this.offset += 4; return n; }
    id(size = 8) { const b = this.b.subarray(this.offset, this.offset + size); this.offset += size; return b; }
    str() { const n = this.int(); return this.id(n).toString('utf8'); }
}
const i = (n: number) => { const b = Buffer.alloc(4); b.writeInt32BE(n); return b; };
const str = (s: string) => { const b = Buffer.from(s); return Buffer.concat([i(b.length), b]); };
const byte = (n: number) => Buffer.from([n]);
const CLASS = 'Lcom/zernova/runtime/core/privileged/processor/chain/AbstractMessageProcessorChain;';
/** Owns one loopback JDWP connection; event threads alone suspend, never the VM. */
export class FlowDebugger {
    private socket?: net.Socket;
    private buffer = Buffer.alloc(0);
    private sequence = 0;
    private handshake = false;
    private pending = new Map<number, {
        resolve: (b: Buffer) => void;
        reject: (e: Error) => void;
        timer: ReturnType<typeof setTimeout>;
    }>();
    private sizes = { field: 8, method: 8, object: 8, type: 8, frame: 8 };
    private paused = new Map<string, Buffer>();
    private breakpoints = new Set<string>();
    private stepThreads = new Set<string>();
    private closing = false;
    private eventQueue = Promise.resolve();
    private installed = new Set<string>();
    constructor(private readonly onStop: (stop: FlowStop) => void, private readonly log: (text: string) => void, private readonly onEnd: () => void) { }
    setBreakpoints(paths: string[]) { this.breakpoints = new Set(paths); }
    async connect(port: number): Promise<void> {
        await new Promise<void>((resolve, reject) => {
            const socket = this.socket = net.createConnection({ host: '127.0.0.1', port });
            const timer = setTimeout(() => { socket.destroy(); reject(new Error('Debugger handshake timed out.')); }, 10000);
            socket.on('connect', () => socket.write('JDWP-Handshake'));
            socket.on('error', reject);
            socket.on('close', () => { clearTimeout(timer); this.fail(new Error('Debugger disconnected.')); this.onEnd(); });
            socket.on('data', (chunk: Buffer) => {
                this.buffer = Buffer.concat([this.buffer, chunk]);
                if (!this.handshake) {
                    if (this.buffer.length < 14)
                        return;
                    if (this.buffer.subarray(0, 14).toString() !== 'JDWP-Handshake') {
                        reject(new Error('Invalid JDWP handshake'));
                        socket.destroy();
                        return;
                    }
                    this.handshake = true;
                    this.buffer = this.buffer.subarray(14);
                    clearTimeout(timer);
                    resolve();
                }
                this.packets();
            });
        });
        const r = new Reader(await this.command(1, 7));
        this.sizes = { field: r.int(), method: r.int(), object: r.int(), type: r.int(), frame: r.int() };
        // ClassPrepare handles a fresh engine; ClassesBySignature handles attach/redeploy.
        await this.command(15, 1, Buffer.concat([byte(8), byte(1), i(1), byte(5), str(CLASS.slice(1, -1).replaceAll('/', '.'))]));
        const classes = new Reader(await this.command(1, 2, str(CLASS)));
        const count = classes.int();
        for (let n = 0; n < count; n++) {
            const tag = classes.byte(), id = classes.id(this.sizes.type);
            classes.int();
            await this.install(tag, id);
        }
        this.log('Flow debugger connected. Send an HTTP request to hit a component breakpoint.\n');
    }
    private packets() { while (this.buffer.length >= 11) {
        const length = this.buffer.readUInt32BE(0);
        if (length < 11 || length > 8 * 1024 * 1024) {
            this.socket?.destroy();
            return;
        }
        if (this.buffer.length < length)
            return;
        const p = this.buffer.subarray(0, length);
        this.buffer = this.buffer.subarray(length);
        const id = p.readUInt32BE(4);
        if (p[8] & 128) {
            const pending = this.pending.get(id);
            if (pending) {
                clearTimeout(pending.timer);
                this.pending.delete(id);
                const error = p.readUInt16BE(9);
                if (error)
                    pending.reject(new Error('JDWP error ' + error));
                else
                    pending.resolve(p.subarray(11));
            }
        }
        else if (p[9] === 64 && p[10] === 100) {
            this.eventQueue = this.eventQueue.then(() => this.events(p.subarray(11))).catch(error => { this.log('Debugger error: ' + String(error) + '\n'); void this.close(); });
        }
    } }
    private command(set: number, command: number, data: Buffer = Buffer.alloc(0)): Promise<Buffer> { return new Promise((resolve, reject) => { if (!this.socket || this.socket.destroyed) {
        reject(new Error('Debugger is disconnected.'));
        return;
    } const id = ++this.sequence, h = Buffer.alloc(11); h.writeUInt32BE(11 + data.length); h.writeUInt32BE(id, 4); h[9] = set; h[10] = command; const timer = setTimeout(() => { this.pending.delete(id); reject(new Error('Debugger command timed out.')); }, 10000); this.pending.set(id, { resolve, reject, timer }); this.socket.write(Buffer.concat([h, data])); }); }
    private async install(tag: number, type: Buffer) {
        if (this.installed.has(type.toString('hex')))
            return;
        const methods = new Reader(await this.command(2, 5, type));
        const count = methods.int();
        for (let n = 0; n < count; n++) {
            const method = methods.id(this.sizes.method), name = methods.str();
            methods.str();
            methods.int();
            if (name !== 'beforeComponentProcessingStrategy')
                continue;
            const lines = new Reader(await this.command(6, 1, Buffer.concat([type, method])));
            const start = lines.id(8);
            lines.id(8);
            const entries = lines.int();
            let index = start;
            if (entries) {
                index = lines.id(8);
                lines.int();
            }
            await this.command(15, 1, Buffer.concat([byte(2), byte(1), i(1), byte(7), byte(tag), type, method, index]));
            this.installed.add(type.toString('hex'));
            return;
        }
        throw new Error('Bundled engine has no supported flow-debug hook.');
    }
    private async events(data: Buffer) {
        const r = new Reader(data), policy = r.byte(), count = r.int();
        for (let n = 0; n < count; n++) {
            const kind = r.byte();
            r.int();
            if (kind === 8) {
                const thread = r.id(this.sizes.object), tag = r.byte(), type = r.id(this.sizes.type);
                r.str();
                r.int();
                await this.install(tag, type);
                if (policy)
                    await this.command(11, 3, thread);
            }
            else if (kind === 2) {
                const thread = r.id(this.sizes.object);
                r.byte();
                r.id(this.sizes.type);
                r.id(this.sizes.method);
                r.id(8);
                const frames = new Reader(await this.command(11, 6, Buffer.concat([thread, i(0), i(1)])));
                if (!frames.int())
                    throw new Error('Missing suspended component frame.');
                const frame = frames.id(this.sizes.frame);
                const value = new Reader(await this.command(16, 1, Buffer.concat([thread, frame, i(1), i(2), byte(115)])));
                value.int();
                value.byte();
                const object = value.id(this.sizes.object);
                let path = '';
                if (object.some(v => v !== 0))
                    path = new Reader(await this.command(10, 1, object)).str();
                const key = thread.toString('hex');
                const stepping = this.stepThreads.delete(key);
                if (this.breakpoints.has(path) || stepping) {
                    this.paused.set(key, thread);
                    this.log('Paused before ' + path + '\n');
                    this.onStop({ path, thread: key });
                }
                else
                    await this.command(11, 3, thread);
            }
            else if (kind === 90) {
                r.id(this.sizes.object);
            }
            else if (kind === 99) {
                this.log('Runtime debugger exited.\n');
            }
            else
                throw new Error('Unexpected debugger event ' + kind);
        }
    }
    async resume(step = false) { for (const [key, thread] of [...this.paused]) {
        if (step)
            this.stepThreads.add(key);
        this.paused.delete(key);
        await this.command(11, 3, thread);
    } this.log(step ? 'Step to next component.\n' : 'Resumed processing.\n'); }
    private fail(error: Error) { for (const p of this.pending.values()) {
        clearTimeout(p.timer);
        p.reject(error);
    } this.pending.clear(); }
    async close() { if (this.closing)
        return; this.closing = true; try {
        await this.command(1, 6);
    }
    catch { /* Dispose resumes all debugger-suspended threads. */ }
    finally {
        this.socket?.destroy();
        this.fail(new Error('Debugger closed.'));
    } }
}
