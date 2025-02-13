package org.redthsgayclub.no7ter.asm.transformers;

import org.redthsgayclub.no7ter.asm.loader.InjectionStatus;
import org.redthsgayclub.no7ter.asm.loader.MWETransformer;
import org.redthsgayclub.no7ter.asm.mappings.FieldMapping;
import org.objectweb.asm.tree.ClassNode;

public class EntityArrowTransformer implements MWETransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.entity.projectile.EntityArrow"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionStatus status) {
        status.setInjectionPoints(0);
        addInterface(classNode, "EntityArrowAccessor");
        classNode.visitField(ACC_PUBLIC, FieldMapping.ENTITYARROW$PINNEDTOPLAYER.name, FieldMapping.ENTITYARROW$PINNEDTOPLAYER.desc, null, 0);
        addGetterMethod(
                classNode,
                "isInGround",
                FieldMapping.ENTITYARROW$ISINGROUND,
                null
        );
        addGetterMethod(
                classNode,
                "isPinnedToPlayer",
                FieldMapping.ENTITYARROW$PINNEDTOPLAYER,
                null
        );
    }

}
