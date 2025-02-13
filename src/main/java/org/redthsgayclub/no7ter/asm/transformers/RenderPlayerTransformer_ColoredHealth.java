package org.redthsgayclub.no7ter.asm.transformers;

import org.redthsgayclub.no7ter.asm.loader.InjectionStatus;
import org.redthsgayclub.no7ter.asm.loader.MWETransformer;
import org.redthsgayclub.no7ter.asm.mappings.ClassMapping;
import org.redthsgayclub.no7ter.asm.mappings.MethodMapping;
import org.objectweb.asm.tree.*;

public class RenderPlayerTransformer_ColoredHealth implements MWETransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.renderer.entity.RenderPlayer"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMethodNode(methodNode, MethodMapping.RENDERPLAYER$RENDEROFFSETLIVINGLABEL)) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (checkMethodInsnNode(insnNode, MethodMapping.SCORE$GETSCOREPOINTS)) {
                        final AbstractInsnNode nextNode = insnNode.getNext();
                        if (checkMethodInsnNode(nextNode, MethodMapping.STRINGBUILDER$APPEND_I)) {
                            final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("RenderPlayerHook_ColoredHealth"), "getColoredScore", "(Ljava/lang/StringBuilder;IL" + ClassMapping.ABSTRACTCLIENTPLAYER + ";)Ljava/lang/StringBuilder;", false));
                            methodNode.instructions.insert(insnNode, list);
                            methodNode.instructions.remove(nextNode);
                            status.addInjection();
                        }
                    }
                }
            }
        }
    }

}
