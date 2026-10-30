#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Restore the route multiplicity contract used by the published AST reader."""
from pathlib import Path
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[1]
API=ROOT/'.zernova-upstream/mule-extensions-api'
OUT=ROOT/'zernova-build-evidence';OUT.mkdir(exist_ok=True)
records=[]
def update(path,addition):
    old=path.read_bytes();text=old.decode();assert 'supportsMultiple' not in text
    at=text.rfind('}');text=text[:at]+addition+text[at:]
    end=text.index('*/')+2
    text=text[:end]+'\n/* ZERNOVA compatibility addition Copyright (c) 2026 ZerNova. CPAL-1.0. */'+text[end:]
    path.write_text(text)
    records.append({'path':str(path.relative_to(API)),'before_sha256':hashlib.sha256(old).hexdigest(),'after_sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
update(API/'mule-extensions-api/src/main/java/org/mule/runtime/extension/api/util/ExtensionModelUtils.java','''
  /**
   * Whether a route can occur more than once. An absent maximum means unbounded;
   * zero and one are not repeated routes. This public signature is consumed by
   * the published 1.6.0 AST reader/serializer used by the 4.10 distribution.
   *
   * @param route route occurrence metadata
   * @return true for an unbounded route or a maximum greater than one
   */
  public static boolean supportsMultiple(NestedRouteModel route) {
    return route.getMaxOccurs().map(maximum -> maximum > 1).orElse(true);
  }
''')
update(API/'mule-extensions-api/src/test/java/org/mule/runtime/extension/api/test/util/ExtensionModelUtilsTestCase.java','''
  @Test
  public void zernovaUnboundedRouteSupportsMultiple() {
    org.mule.runtime.api.meta.model.nested.NestedRouteModel route = mock(org.mule.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.empty());
    assertThat(org.mule.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(true));
  }

  @Test
  public void zernovaSingleRouteDoesNotSupportMultiple() {
    org.mule.runtime.api.meta.model.nested.NestedRouteModel route = mock(org.mule.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.of(1));
    assertThat(org.mule.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(false));
  }

  @Test
  public void zernovaZeroRouteDoesNotSupportMultiple() {
    org.mule.runtime.api.meta.model.nested.NestedRouteModel route = mock(org.mule.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.of(0));
    assertThat(org.mule.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(false));
  }

  @Test
  public void zernovaRepeatedRouteSupportsMultiple() {
    org.mule.runtime.api.meta.model.nested.NestedRouteModel route = mock(org.mule.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.of(2));
    assertThat(org.mule.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(true));
  }
''')
record={'reason':'Actual Choice deployment found a missing static API signature. Constant-pool inspection of all assembled engine JARs identified this one missing ExtensionModelUtils signature, called by AST reader and serializer 1.6.0. Restore the real occurrence rule; do not remove routing or substitute a stub.','files':records,'license':'CPAL-1.0','tests':'Four boundary cases added to the existing utility test class; execution is a separate build stage.'}
(OUT/'route-multiplicity-compatibility.json').write_text(json.dumps(record,indent=2)+'\n')
(OUT/'extensions-api-complete-source.patch').write_bytes(subprocess.check_output(['git','-C',str(API),'diff']))
meta=OUT/'extensions-api-source.json';data=json.loads(meta.read_text());data['java_sources_changed']=True;data['additional_patch']='extensions-api-complete-source.patch';meta.write_text(json.dumps(data,indent=2)+'\n')
print('Restored actual route multiplicity semantics and four unit boundary cases.')
