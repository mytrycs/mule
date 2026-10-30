import assert from 'node:assert/strict';
import { responseGeometry, requestGeometry } from '../src/webview/flowLayout';
assert.equal(responseGeometry({ x: 300, y: 90 }, { x: 100, y: 90 }, 160), 'M 300 90 V 160 H 100 V 90 M 96 95 L 100 90 L 104 95');
assert(responseGeometry({ x: 604, y: 90 }, { x: 100, y: 90 }, 160).startsWith('M 604 90'));
assert(responseGeometry({ x: 300, y: 190 }, { x: 100, y: 90 }, 160).includes('V 206'));
assert.equal(requestGeometry({x:90,y:40},{x:210,y:40}),'M 90 40 L 210 40 M 205 36 L 210 40 L 205 44');
console.log('Flow request/return geometry: 4 checks passed');
