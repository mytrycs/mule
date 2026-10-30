/* Copyright (c) 2026 ZerNova. */
package com.zernova.studio.transform;
import java.util.Collection;import java.util.List;
import com.zernova.runtime.dsl.api.xml.*;
public final class TransformNamespace implements XmlNamespaceInfoProvider {
 public Collection<XmlNamespaceInfo> getXmlNamespacesInfo(){return List.of(new XmlNamespaceInfo(){public String getNamespaceUriPrefix(){return "https://schemas.zernova.dev/transform";}public String getNamespace(){return "zt";}});}
}
