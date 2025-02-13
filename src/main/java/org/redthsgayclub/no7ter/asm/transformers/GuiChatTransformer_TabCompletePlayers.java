package org.redthsgayclub.no7ter.asm.transformers;

import org.redthsgayclub.no7ter.asm.loader.InjectionStatus;
import org.redthsgayclub.no7ter.asm.loader.MWETransformer;
import org.redthsgayclub.no7ter.asm.mappings.FieldMapping;
import org.redthsgayclub.no7ter.asm.mappings.MethodMapping;
import org.objectweb.asm.tree.*;

public class GuiChatTransformer_TabCompletePlayers implements MWETransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.gui.GuiChat"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(2);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMethodNode(methodNode, MethodMapping.GUICHAT$SENDAUTOCOMPLETEREQUEST)) {
                final InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(getNewFieldInsnNode(GETFIELD, FieldMapping.GUICHAT$WAITINGONAUTOCOMPLETE));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ALOAD, 2));
                list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("GuiChatHook"), "autoComplete", "(ZLjava/lang/String;Ljava/lang/String;)Z", false));
                list.add(getNewFieldInsnNode(PUTFIELD, FieldMapping.GUICHAT$WAITINGONAUTOCOMPLETE));
                methodNode.instructions.insert(list);
                status.addInjection();
            }
            if (checkMethodNode(methodNode, MethodMapping.GUICHAT$ONAUTOCOMPLETERESPONSE)) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (checkFieldInsnNode(insnNode, GETSTATIC, FieldMapping.CLIENTCOMMANDHANDLER$INSTANCE)) {
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 1));
                        list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("GuiChatHook"), "getLatestAutoComplete", "([Ljava/lang/String;)[Ljava/lang/String;", false));
                        list.add(new VarInsnNode(ASTORE, 1));
                        methodNode.instructions.insertBefore(insnNode, list);
                        status.addInjection();
                    }
                }
            }
        }
    }

}
