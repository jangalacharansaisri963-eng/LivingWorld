package com.livingworld.mixin;

import com.livingworld.core.LivingWorldEntity;
import com.livingworld.intelligence.MobIntelligence;
import com.livingworld.reaction.LivingWorldBehaviorGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin injecting the Living World intelligence layer and goal steering into vanilla MobEntity.
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements LivingWorldEntity {

    @Shadow
    protected GoalSelector goalSelector;

    @Unique
    private MobIntelligence livingworld$intelligence;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void livingworld$init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        this.livingworld$intelligence = new MobIntelligence(self);
    }

    @Inject(method = "initGoals", at = @At("RETURN"))
    private void livingworld$addGoals(CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        if (this.livingworld$intelligence != null) {
            this.goalSelector.add(1, new LivingWorldBehaviorGoal(self, this.livingworld$intelligence));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void livingworld$onTick(CallbackInfo ci) {
        if (this.livingworld$intelligence != null) {
            this.livingworld$intelligence.tick();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void livingworld$writeCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.livingworld$intelligence != null) {
            NbtCompound tag = new NbtCompound();
            this.livingworld$intelligence.writeToNbt(tag);
            nbt.put("LivingWorld", tag);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void livingworld$readCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.livingworld$intelligence != null && nbt.contains("LivingWorld")) {
            this.livingworld$intelligence.readFromNbt(nbt.getCompound("LivingWorld"));
        }
    }

    @Override
    public MobIntelligence livingworld$getIntelligence() {
        return this.livingworld$intelligence;
    }

    @Override
    public void livingworld$setIntelligence(MobIntelligence intelligence) {
        this.livingworld$intelligence = intelligence;
    }
}
