package org.redthsgayclub.no7ter.asm.transformers;

import org.redthsgayclub.no7ter.asm.loader.InjectionStatus;
import org.redthsgayclub.no7ter.asm.loader.MWETransformer;
import org.redthsgayclub.no7ter.asm.mappings.FieldMapping;
import org.redthsgayclub.no7ter.asm.mappings.MethodMapping;
import org.objectweb.asm.tree.*;

public class EntityRendererTransformer_CancelNightVision implements MWETransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.renderer.EntityRenderer"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(2);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMethodNode(methodNode, MethodMapping.ENTITYRENDERER$UPDATELIGHTMAP) || checkMethodNode(methodNode, MethodMapping.ENTITYRENDERER$UPDATEFOGCOLOR)) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (checkFieldInsnNode(insnNode, GETSTATIC, FieldMapping.POTION$NIGHTVISION)) {
                        final AbstractInsnNode secondNode = insnNode.getNext();
                        if (secondNode instanceof MethodInsnNode && secondNode.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) secondNode).name.equals(MethodMapping.ENTITYLIVINGBASE$ISPOTIONACTIVE.name)) {
                            final AbstractInsnNode thirdNode = secondNode.getNext();
                            if (checkJumpInsnNode(thirdNode, IFEQ)) {
                                final LabelNode labelNode = ((JumpInsnNode) thirdNode).label;
                                final InsnList list = new InsnList();
                                list.add(new JumpInsnNode(IFEQ, labelNode));
                                list.add(getNewConfigFieldInsnNode("cancelNightVisionEffect"));
                                methodNode.instructions.insert(secondNode, list);
                                status.addInjection();
                            }
                        }
                    }
                }
            }
        }
    }

}
