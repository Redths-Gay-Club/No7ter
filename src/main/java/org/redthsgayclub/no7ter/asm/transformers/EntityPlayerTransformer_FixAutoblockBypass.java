package org.redthsgayclub.no7ter.asm.transformers;

import org.redthsgayclub.no7ter.asm.loader.InjectionStatus;
import org.redthsgayclub.no7ter.asm.loader.MWETransformer;
import org.redthsgayclub.no7ter.asm.mappings.ClassMapping;
import org.redthsgayclub.no7ter.asm.mappings.FieldMapping;
import org.redthsgayclub.no7ter.asm.mappings.MethodMapping;
import org.objectweb.asm.tree.*;

public class EntityPlayerTransformer_FixAutoblockBypass implements MWETransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.entity.player.EntityPlayer"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMethodNode(methodNode, MethodMapping.ENTITYPLAYER$ONUPDATE)) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (checkVarInsnNode(insnNode, ALOAD, 1)) {
                        final AbstractInsnNode secondNode = insnNode.getNext();
                        if (checkVarInsnNode(secondNode, ALOAD, 0)) {
                            final AbstractInsnNode thirdNode = secondNode.getNext();
                            if (checkFieldInsnNode(thirdNode, GETFIELD, FieldMapping.ENTITYPLAYER$ITEMINUSE)) {
                                final AbstractInsnNode fourthNode = thirdNode.getNext();
                                if (checkJumpInsnNode(fourthNode, IF_ACMPNE)) {
                                    final InsnList list = new InsnList();
                                    list.add(new VarInsnNode(ALOAD, 0));
                                    list.add(new MethodInsnNode(
                                            INVOKESTATIC,
                                            getHookClass("EntityPlayerHook_FixAutoblockBypass"),
                                            "areItemStackSemiEquals",
                                            "(L" + ClassMapping.ITEMSTACK + ";L" + ClassMapping.ITEMSTACK + ";L" + ClassMapping.ENTITYPLAYER + ";)Z",
                                            false));
                                    methodNode.instructions.insert(thirdNode, list);
                                    ((JumpInsnNode) fourthNode).setOpcode(IFEQ);
                                    status.addInjection();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
