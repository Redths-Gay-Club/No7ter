package org.redthsgayclub.no7ter.asm.transformers;

import org.redthsgayclub.no7ter.asm.loader.InjectionStatus;
import org.redthsgayclub.no7ter.asm.loader.MWETransformer;
import org.redthsgayclub.no7ter.asm.mappings.ClassMapping;
import org.redthsgayclub.no7ter.asm.mappings.MethodMapping;
import org.objectweb.asm.tree.*;

public class EntityOtherPlayerMPTransformer_PositionTracker implements MWETransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.entity.EntityOtherPlayerMP"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMethodNode(methodNode, MethodMapping.ENTITY$SETPOSITIONANDROTATION2)) {
                final InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(DLOAD, 1));
                list.add(new VarInsnNode(DLOAD, 3));
                list.add(new VarInsnNode(DLOAD, 5));
                list.add(new VarInsnNode(FLOAD, 7));
                list.add(new VarInsnNode(FLOAD, 8));
                list.add(new MethodInsnNode(
                        INVOKESTATIC,
                        getHookClass("EntityOtherPlayerMPHook"),
                        "setPositionAndRotation",
                        "(L" + ClassMapping.ENTITYOTHERPLAYERMP + ";DDDFF)V",
                        false
                ));
                methodNode.instructions.insert(list);
                status.addInjection();
            }
        }
    }

}
