package org.redthsgayclub.no7ter.asm.transformers;

import org.redthsgayclub.no7ter.asm.loader.InjectionStatus;
import org.redthsgayclub.no7ter.asm.loader.MWETransformer;
import org.redthsgayclub.no7ter.asm.mappings.ClassMapping;
import org.redthsgayclub.no7ter.asm.mappings.MethodMapping;
import org.objectweb.asm.tree.*;

public class EntityFXTransformer_ClearView implements MWETransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.particle.EntityFX"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMethodNode(methodNode, MethodMapping.ENTITYFX$RENDERPARTICLE)) {
                /*
                 * Injects at HEAD:
                 * if(EntityFXHook.shouldHideParticle(this, entityIn)) {
                 *    return;
                 * }
                 */
                methodNode.instructions.insert(hideParticleInsnList());
                status.addInjection();
                break;
            }
        }
    }

    private InsnList hideParticleInsnList() {
        final InsnList list = new InsnList();
        final LabelNode notCanceled = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("EntityFXHook"), "shouldHideParticle", "(L" + ClassMapping.ENTITYFX + ";L" + ClassMapping.ENTITY + ";)Z", false));
        list.add(new JumpInsnNode(IFEQ, notCanceled));
        list.add(new InsnNode(RETURN));
        list.add(notCanceled);
        return list;
    }

}
