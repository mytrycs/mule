"""Exercise JVM linkage, service metadata and recursive archive relocation."""
import importlib.util
import io
from pathlib import Path
import subprocess
import tempfile
import unittest
import zipfile

SPEC = importlib.util.spec_from_file_location('relocation', Path(__file__).parents[1] / 'refactor_namespace.py')
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class NamespaceRelocationTest(unittest.TestCase):
    def setUp(self):
        self.relocation = MODULE.Relocation({'MuleProbe': 'ZernovaProbe', 'DefaultMuleProbe': 'DefaultZernovaProbe'})

    def test_application_format_contract_and_idempotence(self):
        relocation = MODULE.Relocation({}, application_format=True)
        original = '<m:mule xmlns:m="http://www.mulesoft.org/schema/mule/core"><m:flow/></m:mule>'
        converted = relocation.text(original)
        self.assertEqual(converted, '<m:zernova xmlns:m="https://schemas.zernova.dev/core"><m:flow/></m:zernova>')
        self.assertEqual(relocation.text('<mule/>'), '<zernova/>')
        self.assertEqual(relocation.text(converted), converted)
        self.assertEqual(relocation.text('MULE-1234'), 'MULE-1234')
        self.assertEqual(relocation.text('META-INF/mule-artifact/mule-artifact.json'),
                         'META-INF/zernova-artifact/zernova-artifact.json')
        self.assertEqual(relocation.text('http\\://www.mulesoft.org/schema/mule/core/current/mule.xsd'),
                         'https\\://schemas.zernova.dev/core/current/zernova.xsd')

    def test_application_contract_bytecode_without_legacy_java_packages(self):
        relocation = MODULE.Relocation({}, application_format=True)
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            source = root / 'Contract.java'
            source.write_text('public class Contract { public static String getMinMuleVersion() { return "mule"; } public static void main(String[] args) throws Exception { System.out.print(Contract.class.getMethod(args[0]).invoke(null)); } }')
            subprocess.run(['javac', '--release', '17', str(source)], check=True, capture_output=True)
            bytecode = root / 'Contract.class'
            bytecode.write_bytes(relocation.classfile(bytecode.read_bytes()))
            result = subprocess.run(['java', '-cp', str(root), 'Contract', 'getMinZernovaVersion'], check=True, capture_output=True, text=True)
            self.assertEqual(result.stdout, 'zernova')

    def test_packaging_enum_derived_names_and_callers(self):
        relocation = MODULE.Relocation({}, application_format=True)
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            folder = root / 'com/zernova/tools/api/packager/packaging'
            folder.mkdir(parents=True)
            (folder / 'PackagingType.java').write_text('package com.zernova.tools.api.packager.packaging; public enum PackagingType { MULE_APPLICATION, MULE_DOMAIN; public String toString() { return name().toLowerCase(java.util.Locale.ROOT).replace("_", "-"); } }')
            (root / 'Probe.java').write_text('import com.zernova.tools.api.packager.packaging.PackagingType; public class Probe { public static void main(String[] args) { System.out.print(PackagingType.MULE_APPLICATION + ":" + PackagingType.valueOf(args[0])); } }')
            subprocess.run(['javac', '--release', '17', str(folder / 'PackagingType.java'), str(root / 'Probe.java')], check=True, capture_output=True)
            for path in root.rglob('*.class'):
                path.write_bytes(relocation.classfile(path.read_bytes()))
            result = subprocess.run(['java', '-cp', str(root), 'Probe', 'ZERNOVA_APPLICATION'], check=True, capture_output=True, text=True)
            self.assertEqual(result.stdout, 'zernova-application:zernova-application')

    def test_descriptor_paths_modules_and_coordinates(self):
        for old, new in [
            ('Lorg/mule/runtime/MuleProbe;', 'Lcom/zernova/runtime/ZernovaProbe;'),
            ('org.mule.runtime:artifact:1.0', 'com.zernova.runtime:artifact:1.0'),
            ('requires org.mule.runtime;', 'requires com.zernova.runtime;'),
            ('META-INF/services/org.mule.DefaultMuleProbe', 'META-INF/services/com.zernova.DefaultZernovaProbe'),
        ]:
            self.assertEqual(self.relocation.text(old), new)
        self.assertEqual(self.relocation.text('http://www.mulesoft.org/schema/mule/core'),
                         'http://www.mulesoft.org/schema/mule/core')
        self.assertEqual(self.relocation.text('http://www.mulesoft.org/mule-guide'),
                         'http://www.mulesoft.org/mule-guide')
        self.assertEqual(self.relocation.text(r'\u0027org.mule.runtime.MuleProbe\u0027'),
                         r'\u0027com.zernova.runtime.ZernovaProbe\u0027')

    def test_actual_jvm_class_linkage_reflection_and_modified_utf8(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            source = root / 'MuleProbe.java'
            source.write_text('''package org.mule.runtime;
public class MuleProbe {
  public static void main(String[] args) throws Exception {
    MuleProbe value = new MuleProbe();
    if (!value.getClass().equals(Class.forName("org.mule.runtime.MuleProbe"))) throw new AssertionError();
    System.out.print("héllo\\u0000🚀:" + value.getClass().getName());
  }
}''')
            subprocess.run(['javac', '--release', '17', '-d', str(root), str(source)], check=True)
            old = root / 'org/mule/runtime/MuleProbe.class'
            new = root / 'com/zernova/runtime/ZernovaProbe.class'
            new.parent.mkdir(parents=True)
            new.write_bytes(self.relocation.classfile(old.read_bytes()))
            old.unlink()
            run = subprocess.run(['java', '-Xverify:all', '-cp', str(root), 'com.zernova.runtime.ZernovaProbe'],
                                 check=True, capture_output=True, text=True)
            self.assertEqual(run.stdout, 'héllo\0🚀:com.zernova.runtime.ZernovaProbe')

    def test_nested_archives_service_files_and_immutable_license(self):
        inner = io.BytesIO()
        license_bytes = b'Original MuleSoft copyright. org.mule.runtime attribution.'
        with zipfile.ZipFile(inner, 'w') as z:
            z.writestr('META-INF/services/org.mule.runtime.MuleProbe', 'org.mule.runtime.DefaultMuleProbe\n')
            z.writestr('META-INF/LICENSE.txt', license_bytes)
            z.writestr('META-INF/VENDOR.SF', b'stale signature')
        outer = io.BytesIO()
        with zipfile.ZipFile(outer, 'w') as z:
            z.writestr('repository/org/mule/service.jar', inner.getvalue())
        output = self.relocation.archive(outer.getvalue())
        with zipfile.ZipFile(io.BytesIO(output)) as z:
            with zipfile.ZipFile(io.BytesIO(z.read('repository/com/zernova/service.jar'))) as nested:
                self.assertEqual(nested.read('META-INF/services/com.zernova.runtime.ZernovaProbe'),
                                 b'com.zernova.runtime.DefaultZernovaProbe\n')
                self.assertEqual(nested.read('META-INF/LICENSE.txt'), license_bytes)
                self.assertNotIn('META-INF/VENDOR.SF', nested.namelist())
        self.assertEqual(self.relocation.archive(output), output)

    def test_folded_manifest_names_and_invalidated_digests(self):
        original = b'Automatic-Module-Name: org.mule.run\r\n time\r\nSHA-256-Digest: abc\r\n\r\n'
        result = self.relocation.manifest(original)
        self.assertIn(b'Automatic-Module-Name: com.zernova.runtime', result)
        self.assertNotIn(b'Digest', result)

    def test_rejects_invalid_classes(self):
        with self.assertRaises(ValueError):
            self.relocation.classfile(b'not a class')

    def test_sisu_and_spring_provider_indexes_and_maven_poms(self):
        for name in ['META-INF/sisu/javax.inject.Named', 'META-INF/spring.factories', 'artifact-1.0.pom']:
            self.assertEqual(self.relocation.content(name, b'org.mule.runtime.MuleProbe'),
                             b'com.zernova.runtime.ZernovaProbe')

    def test_unrelated_signed_archive_is_preserved_byte_for_byte(self):
        original = io.BytesIO()
        with zipfile.ZipFile(original, 'w') as z:
            z.writestr('META-INF/MANIFEST.MF', b'Manifest-Version: 1.0\r\nSHA-256-Digest: abc\r\n\r\n')
            z.writestr('META-INF/VENDOR.SF', b'original signature')
            z.writestr('data.txt', 'untouched')
        self.assertEqual(self.relocation.archive(original.getvalue()), original.getvalue())

    def test_serialized_descriptor_relocation_is_readable_by_jvm(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            source = root / 'MuleProbe.java'
            source.write_text('''package org.mule.runtime;
import java.io.*;
public class MuleProbe implements Serializable {
  private static final long serialVersionUID = 1L;
  public static void main(String[] args) throws Exception {
    if (args[0].equals("write")) {
      try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(args[1]))) {
        out.writeObject(new MuleProbe());
      }
    } else {
      try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[1]))) {
        System.out.print(in.readObject().getClass().getName());
      }
    }
  }
}''')
            subprocess.run(['javac', '--release', '17', '-d', str(root), str(source)], check=True)
            fixture = root / 'fixture.ser'
            subprocess.run(['java', '-cp', str(root), 'org.mule.runtime.MuleProbe', 'write', str(fixture)], check=True)
            fixture.write_bytes(self.relocation.serialized_fixture(fixture.read_bytes()))
            old = root / 'org/mule/runtime/MuleProbe.class'
            new = root / 'com/zernova/runtime/ZernovaProbe.class'
            new.parent.mkdir(parents=True)
            new.write_bytes(self.relocation.classfile(old.read_bytes()))
            old.unlink()
            run = subprocess.run(['java', '-cp', str(root), 'com.zernova.runtime.ZernovaProbe', 'read', str(fixture)],
                                 check=True, capture_output=True, text=True)
            self.assertEqual(run.stdout, 'com.zernova.runtime.ZernovaProbe')


if __name__ == '__main__':
    unittest.main()
