import test from 'node:test';
import assert from 'node:assert/strict';
import { insertionAt, moveToInsertion } from '../src/shared/placement';
test('Open canvas points resolve before, between and after components', () => {
  assert.equal(insertionAt([],100),0);
  assert.equal(insertionAt([100,300,500],50),0);
  assert.equal(insertionAt([100,300,500],200),1);
  assert.equal(insertionAt([100,300,500],400),2);
  assert.equal(insertionAt([100,300,500],600),3);
});
test('Dragging an existing component accounts for its removed position', () => {
  assert.equal(moveToInsertion(0,2,3),1); // A after B: B A C
  assert.equal(moveToInsertion(0,1,3),0); // A before B is unchanged
  assert.equal(moveToInsertion(0,3,3),2); // A after C: B C A
  assert.equal(moveToInsertion(2,1,3),1); // C before B: A C B
});
