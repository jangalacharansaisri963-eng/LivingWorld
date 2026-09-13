package com.livingworld.mixin;

import com.livingworld.core.LivingWorldEntity;
import com.livingworld.intelligence.MobIntelligence;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin injecting into LivingEntity's damage method to notify the Living World intelligence layer.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void livingworld$onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && (Object) this instanceof LivingWorldEntity lwe) {
            MobIntelligence intelligence = lwe.livingworld$getIntelligence();
            if (intelligence != null) {
                intelligence.onDamage(source, amount, source.getAttacker());
            }
        }
    }
}
