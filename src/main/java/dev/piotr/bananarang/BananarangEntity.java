package dev.piotr.bananarang;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BananarangEntity extends ThrowableItemProjectile {
	// The banana flies a circular arc: turning TURN_PER_TICK each tick at
	// FLIGHT_SPEED traces a full 360° circle in ARC_TICKS (~15 blocks across),
	// curving left like a right-handed boomerang throw. After the loop it
	// homes in on the thrower in case they moved.
	private static final int ARC_TICKS = 40;
	private static final float TURN_PER_TICK = (float) (2.0 * Math.PI / ARC_TICKS);
	private static final int MAX_LIFETIME_TICKS = 200;
	private static final float DAMAGE = 10.0F;
	private static final double FLIGHT_SPEED = 1.2;
	private static final double PICKUP_RANGE = 1.5;

	private int age;
	private boolean returning;

	public BananarangEntity(EntityType<? extends BananarangEntity> type, Level level) {
		super(type, level);
	}

	public BananarangEntity(ServerLevel level, LivingEntity owner, ItemStack stack) {
		super(Bananarang.BANANA_ENTITY, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Bananarang.BANANA;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0;
	}

	@Override
	protected void onHit(HitResult result) {
		// A boomerang doesn't stop for anything: blocks are smashed and
		// entities damaged in tick(), so swallow vanilla hit handling
		// (which would discard the projectile).
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		age++;

		smashBlocks(serverLevel);
		hitEntities(serverLevel);

		if (!returning && age >= ARC_TICKS) {
			returning = true;
		}

		if (returning) {
			Entity owner = getOwner();
			if (owner == null || !owner.isAlive() || age > MAX_LIFETIME_TICKS) {
				spawnAtLocation(serverLevel, getItem());
				discard();
				return;
			}
			Vec3 target = owner.position().add(0.0, owner.getBbHeight() / 2.0, 0.0);
			Vec3 toOwner = target.subtract(position());
			if (toOwner.length() < PICKUP_RANGE) {
				if (!(owner instanceof Player player) || !player.addItem(getItem().copy())) {
					spawnAtLocation(serverLevel, getItem());
				}
				discard();
				return;
			}
			setDeltaMovement(toOwner.normalize().scale(FLIGHT_SPEED));
		} else {
			Vec3 velocity = getDeltaMovement();
			if (velocity.lengthSqr() > 1.0E-4) {
				// rotate the velocity to fly the arc; renormalizing also cancels
				// the base class's per-tick drag. The vertical component eases
				// toward level flight so steep throws flatten out.
				setDeltaMovement(velocity.yRot(TURN_PER_TICK)
						.multiply(1.0, 0.9, 1.0)
						.normalize().scale(FLIGHT_SPEED));
			}
		}
	}

	private void smashBlocks(ServerLevel level) {
		AABB reach = getBoundingBox().inflate(0.75);
		for (BlockPos pos : BlockPos.betweenClosed(reach)) {
			BlockState state = level.getBlockState(pos);
			if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0.0F) {
				level.destroyBlock(pos.immutable(), true, this, 512);
			}
		}
	}

	private void hitEntities(ServerLevel level) {
		Entity owner = getOwner();
		AABB reach = getBoundingBox().inflate(0.5);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, reach,
				entity -> entity != owner && entity.isAlive())) {
			target.hurtServer(level, damageSources().thrown(this, owner), DAMAGE);
		}
	}
}
