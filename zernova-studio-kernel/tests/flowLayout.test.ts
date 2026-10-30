import assert from 'node:assert/strict';
import { responseGeometry } from '../src/webview/flowLayout';
assert.equal(responseGeometry({ x: 300, y: 90 }, { x: 100, y: 90 }, 160), 'M 300 90 V 160 H 100 V 95 M 96 100 L 100 95 L 104 100');
assert(responseGeometry({ x: 604, y: 90 }, { x: 100, y: 90 }, 160).startsWith('M 604 90'));
assert(responseGeometry({ x: 300, y: 190 }, { x: 100, y: 90 }, 160).includes('V 206'));
console.log('Flow return geometry: 3 checks passed');
