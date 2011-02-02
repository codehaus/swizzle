/**
 *
 * Copyright 2006 David Blevins
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.codehaus.swizzle.depreport;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.Label;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureReader;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev: 602704 $ $Date: 2007-12-09 09:58:22 -0800 (Sun, 09 Dec 2007) $
 */
public class DependencyVisitor implements AnnotationVisitor, SignatureVisitor, ClassVisitor, FieldVisitor, MethodVisitor {
    Set<String> classes = new HashSet<String>();

    Set<String> packages = new HashSet<String>();

    Map<String, Map<String, Integer>> groups = new HashMap<String, Map<String, Integer>>();

    Map<String, Integer> current;

    public Map<String, Map<String, Integer>> getGlobals() {
        return groups;
    }

    public Set<String> getPackages() {
        return packages;
    }

    public Set<String> getClasses() {
        return classes;
    }

    // ClassVisitor

    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        String p = getGroupKey(name);
        current = groups.get(p);
        if (current == null) {
            current = new HashMap<String, Integer>();
            groups.put(p, current);
        }

        if (signature == null) {
            addName(superName);
            addNames(interfaces);
        } else {
            addSignature(signature);
        }
    }

    public AnnotationVisitor visitAnnotation(
            final String desc,
            final boolean visible) {
        addDesc(desc);
        return this;
    }

    public void visitAttribute(final Attribute attr) {
    }

    public FieldVisitor visitField(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final Object value) {
        if (signature == null) {
            addDesc(desc);
        } else {
            addTypeSignature(signature);
        }
        if (value instanceof Type) {
            addType((Type) value);
        }
        return this;
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final String[] exceptions) {
        if (signature == null) {
            addMethodDesc(desc);
        } else {
            addSignature(signature);
        }
        addNames(exceptions);
        return this;
    }

    public void visitSource(final String source, final String debug) {
    }

    public void visitInnerClass(
            final String name,
            final String outerName,
            final String innerName,
            final int access) {
        // addName( outerName);
        // addName( innerName);
    }

    public void visitOuterClass(
            final String owner,
            final String name,
            final String desc) {
        // addName(owner);
        // addMethodDesc(desc);
    }

    // MethodVisitor

    public AnnotationVisitor visitParameterAnnotation(
            final int parameter,
            final String desc,
            final boolean visible) {
        addDesc(desc);
        return this;
    }

    public void visitTypeInsn(final int opcode, final String desc) {
        if (desc.charAt(0) == '[') {
            addDesc(desc);
        } else {
            addName(desc);
        }
    }

    public void visitFieldInsn(
            final int opcode,
            final String owner,
            final String name,
            final String desc) {
        addName(owner);
        addDesc(desc);
    }

    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String desc) {
        addName(owner);
        addMethodDesc(desc);
    }

    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            addType((Type) cst);
        }
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        addDesc(desc);
    }

    public void visitLocalVariable(
            final String name,
            final String desc,
            final String signature,
            final Label start,
            final Label end,
            final int index) {
        addTypeSignature(signature);
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return this;
    }

    public void visitCode() {
    }

    public void visitFrame(
            final int type,
            final int nLocal,
            final Object[] local,
            final int nStack,
            final Object[] stack) {
    }

    public void visitInsn(final int opcode) {
    }

    public void visitIntInsn(final int opcode, final int operand) {
    }

    public void visitVarInsn(final int opcode, final int var) {
    }

    public void visitJumpInsn(final int opcode, final Label label) {
    }

    public void visitLabel(final Label label) {
    }

    public void visitIincInsn(final int var, final int increment) {
    }

    public void visitTableSwitchInsn(
            final int min,
            final int max,
            final Label dflt,
            final Label[] labels) {
    }

    public void visitLookupSwitchInsn(
            final Label dflt,
            final int[] keys,
            final Label[] labels) {
    }

    public void visitTryCatchBlock(
            final Label start,
            final Label end,
            final Label handler,
            final String type) {
        addName(type);
    }

    public void visitLineNumber(final int line, final Label start) {
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
    }

    // AnnotationVisitor

    public void visit(final String name, final Object value) {
        if (value instanceof Type) {
            addType((Type) value);
        }
    }

    public void visitEnum(
            final String name,
            final String desc,
            final String value) {
        addDesc(desc);
    }

    public AnnotationVisitor visitAnnotation(
            final String name,
            final String desc) {
        addDesc(desc);
        return this;
    }

    public AnnotationVisitor visitArray(final String name) {
        return this;
    }

    // SignatureVisitor

    public void visitFormalTypeParameter(final String name) {
    }

    public SignatureVisitor visitClassBound() {
        return this;
    }

    public SignatureVisitor visitInterfaceBound() {
        return this;
    }

    public SignatureVisitor visitSuperclass() {
        return this;
    }

    public SignatureVisitor visitInterface() {
        return this;
    }

    public SignatureVisitor visitParameterType() {
        return this;
    }

    public SignatureVisitor visitReturnType() {
        return this;
    }

    public SignatureVisitor visitExceptionType() {
        return this;
    }

    public void visitBaseType(final char descriptor) {
    }

    public void visitTypeVariable(final String name) {
        // TODO verify
    }

    public SignatureVisitor visitArrayType() {
        return this;
    }

    public void visitClassType(final String name) {
        addName(name);
    }

    public void visitInnerClassType(final String name) {
        addName(name);
    }

    public void visitTypeArgument() {
    }

    public SignatureVisitor visitTypeArgument(final char wildcard) {
        return this;
    }

    // common

    public void visitEnd() {
    }

    // ---------------------------------------------

    private String getGroupKey(String name) {
        classes.add(name);
        int n = name.lastIndexOf('/');
        if (n > -1) {
            name = name.substring(0, n);
        }
        name = name.replace('/','.');
        packages.add(name);
        return name;
    }

    private void addName(final String name) {
        if (name == null) {
            return;
        }
        String p = getGroupKey(name);
        if (current.containsKey(p)) {
            current.put(p, current.get(p) + 1);
        } else {
            current.put(p, 1);
        }
    }

    private void addNames(final String[] names) {
        for (int i = 0; names != null && i < names.length; i++) {
            addName(names[i]);
        }
    }

    private void addDesc(final String desc) {
        addType(Type.getType(desc));
    }

    private void addMethodDesc(final String desc) {
        addType(Type.getReturnType(desc));
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            addType(types[i]);
        }
    }

    private void addType(final Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                addType(t.getElementType());
                break;
            case Type.OBJECT:
                addName(t.getClassName().replace('.', '/'));
                break;
        }
    }

    private void addSignature(final String signature) {
        if (signature != null) {
            new SignatureReader(signature).accept(this);
        }
    }

    private void addTypeSignature(final String signature) {
        if (signature != null) {
            new SignatureReader(signature).acceptType(this);
        }
    }
}
