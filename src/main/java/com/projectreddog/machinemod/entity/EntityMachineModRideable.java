package com.projectreddog.machinemod.entity;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.projectreddog.machinemod.init.ModBlocks;
import com.projectreddog.machinemod.init.ModItems;
import com.projectreddog.machinemod.init.ModNetwork;
import com.projectreddog.machinemod.network.MachineModMessageEntityInventoryChangedToClient;
import com.projectreddog.machinemod.network.MachineModMessageEntityToClient;
import com.projectreddog.machinemod.network.MachineModMessageRequestAllInventoryToServer;
import com.projectreddog.machinemod.reference.Reference;
import com.projectreddog.machinemod.utility.BlockUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMachineModRideable extends Entity implements IInventory {

	public double velocity;
	public float yaw;
	protected ItemStack[] inventory;
	public boolean shouldSendClientInvetoryUpdates = false;
	public int tickssincelastbroadcast = 0;

	public boolean isPlayerAccelerating = false;
	public boolean isPlayerBreaking = false;
	public boolean isPlayerTurningRight = false;
	public boolean isPlayerTurningLeft = false;
	public boolean isPlayerPushingSprintButton = false;
	public boolean isPlayerPushingJumpButton = false;
	// additions in 1.10.x version
	public boolean isPlayerPushingSegment1Up = false; // default numpad 7
	public boolean isPlayerPushingSegment1Down = false; // default numpad 1
	public boolean isPlayerPushingSegment2Up = false; // default numpad 8
	public boolean isPlayerPushingSegment2Down = false;// default numpad 2
	public boolean isPlayerPushingSegment3Up = false;// default numpad 9
	public boolean isPlayerPushingSegment3Down = false;// default numpad 3

	public boolean isPlayerPushingTurretRight = false;// default numpad 6
	public boolean isPlayerPushingTurretLeft = false;// default numpad 7

	public double TargetposX;
	public double TargetposY;
	public double TargetposZ;
	public float TargetYaw;
	public boolean isFristTick = true;
	public double lastPosX = 0d;
	public double lastPosY = 0d;
	public double lastPosZ = 0d;
	public float lastAttribute1 = 0f;
	public float lastYaw = 0;
	public int lastCurrentFuelLevel = 0;
	public int sendInterval = 0;
	public double maxSpeed = 0.2d;
	public float Attribute1;// multi-purpose variable use defined in extended
	public float Attribute2;// multi-purpose variable use defined in extended

	public AxisAlignedBB BoundingBox;
	public int ticksSinceLastParticle;
	public int nextParticleAtTick = 20;

	protected double mountedOffsetY = 0d;
	protected double mountedOffsetX = 0d;
	protected double mountedOffsetZ = 0d;
	protected float maxAngle = 0;
	protected float minAngle = 0;
	public double accelerationAmount = .02d;

	protected Item droppedItem = null;

	public int currentFuelLevel = 0;
	public int maxFuelLevel = 1000;

	public boolean willSink = true;
	public boolean isWaterOnly = false;
	public boolean canFly = false;
	public int runTimeTillNextFuelUsage = 20;
	public int maxRunTimeTillNextFuelUsage = 20;
	public int clientTicksSinceLastServerPulse = 0;
	public double turnRate = 1.5d;

	public EntityMachineModRideable(World world) {
		super(world);
		setSize(1.5F, 0.6F); // should be overridden in Extened version.
		this.stepHeight = 1F;
		inventory = new ItemStack[0];

	}

	public void doParticleEffects() {

	}

	public void clientInit() {
		if (worldObj.isRemote) {
			// client side so request inventory
			if (shouldSendClientInvetoryUpdates) {
				ModNetwork.simpleNetworkWrapper.sendToServer((new MachineModMessageRequestAllInventoryToServer(this.getEntityId())));
			}

		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		// testing with always TRUE is in range to render dist to see if it fixes invs entities
		return true;
	}

	public double getMaxVelocity() {
		// created as method so extending class can easily override to allow for
		// different speeds per machine
		return maxSpeed;
	}

	// 1.8
	// @Override
	// public AxisAlignedBB getBoundingBox(){
	// return this.BoundingBox;
	// }

	// 1.8
	// @Override
	// public AxisAlignedBB getCollisionBox(Entity entity){
	// if (entity != riddenByEntity){
	// return entity.boundingBox;
	// }
	// else{
	// return null;// do not colide with the rider
	// }
	// }

	@Override
	public boolean canBeCollidedWith() {
		return !isDead;
	}

	public Item getItemToBeDropped() {
		// need to drop additional items from the inventory of the item
		Random rand = new Random();

		for (int i = 0; i < this.getSizeInventory(); i++) {
			ItemStack item = this.getStackInSlot(i);

			if (item != null && item.stackSize > 0) {
				float rx = rand.nextFloat() * 0.8F + 0.1F;
				float ry = rand.nextFloat() * 0.8F + 0.1F;
				float rz = rand.nextFloat() * 0.8F + 0.1F;

				EntityItem entityItem = new EntityItem(worldObj, posX + rx, posY + ry, posZ + rz, item);

				if (item.hasTagCompound()) {
					entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
				}

				float factor = 0.05F;
				entityItem.motionX = rand.nextGaussian() * factor;
				entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
				entityItem.motionZ = rand.nextGaussian() * factor;
				worldObj.spawnEntityInWorld(entityItem);
				// item.stackSize = 0;
				this.setInventorySlotContents(i, null);

			}
		}
		return droppedItem;

	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) // should be proper class
	{
		if (!worldObj.isRemote && getControllingPassenger() == null) {
			// server side and no rider
			if (player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() == ModItems.fuelcan && player.getHeldItem(EnumHand.MAIN_HAND).getItemDamage() < player.getHeldItem(EnumHand.MAIN_HAND).getMaxDamage()) {
				// player holding a fuel can & it has fuel in it so put fuel into the machine !
				if (this.currentFuelLevel < maxFuelLevel) {
					// can hold more fuel.
					// calc remaining fuel in can see if it is = or > than the remaining fuel storage of this machine
					int amountInCan = (player.getHeldItem(EnumHand.MAIN_HAND).getMaxDamage() - player.getHeldItem(EnumHand.MAIN_HAND).getItemDamage());
					int roomInEntityTank = this.maxFuelLevel - this.currentFuelLevel;
					if (amountInCan > roomInEntityTank) {
						if (!player.capabilities.isCreativeMode) {
							player.getHeldItem(EnumHand.MAIN_HAND).setItemDamage(player.getHeldItem(EnumHand.MAIN_HAND).getMaxDamage() - (amountInCan - roomInEntityTank));
						}
						// will fill machine completely !
						this.currentFuelLevel = this.maxFuelLevel;
					} else {
						// can will be empty becuase entity can hold 100% of the fuel from the can :O
						if (!player.capabilities.isCreativeMode) {

							player.getHeldItem(EnumHand.MAIN_HAND).setItemDamage(player.getHeldItem(EnumHand.MAIN_HAND).getMaxDamage());
						}
						this.currentFuelLevel = this.currentFuelLevel + amountInCan;
					}
				}

			} else {
				if (player.isSneaking()) {
					if (getItemToBeDropped() != null) {
						this.dropItem(getItemToBeDropped(), 1);
						this.setDead();
						// this.addedToChunk = true;
					}

				} else {

					// player.mountEntity(this);
					player.startRiding(this);
				}
			}
		} else if (worldObj.isRemote && this.getControllingPassenger() == null) {
			if (player.isSneaking()) {
				// if (getItemToBeDropped() != null) {

				this.setDead();
				// this.worldObj.removeEntity(this);
				// this.worldObj.getChunkFromChunkCoords(this.chunkCoordX, this.chunkCoordZ).removeEntity(this);
				// this.addedToChunk = true;

				// }
			}

		}
		return true;
	}

	@Override
	public double getMountedYOffset() {
		// should be overridden in extended class if not default;
		return this.height * mountedOffsetY;
	}

	public double getMountedXOffset() {
		// should be overridden in extended class if not default;
		return calcOffsetX(mountedOffsetX);
	}

	public double getMountedZOffset() {
		// should be overridden in extended class if not default;
		return calcOffsetZ(mountedOffsetZ);
	}

	public void updateServer() {

		// New for gravity
		// this.motionY -= 0.03999999910593033D;
		// if (onGround){
		//
		// this.motionY *= -0.5D;
		//
		// }

		// need to reset vars because player is no longer riding in the machine
		// should cause it to stop & not star when the re-enter if they leave
		// while the machine is moving

		if (isWaterOnly && (!(worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))).getBlock().getMaterial(worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d)))) == Material.WATER))) {
			isPlayerAccelerating = false;
			isPlayerBreaking = false;
			isPlayerTurningRight = false;
			isPlayerTurningLeft = false;
			isPlayerPushingSprintButton = false;
			isPlayerPushingJumpButton = false;
		}

		if (this.getControllingPassenger() == null) {
			isPlayerAccelerating = false;
			isPlayerBreaking = false;
			isPlayerTurningRight = false;
			isPlayerTurningLeft = false;
			isPlayerPushingSprintButton = false;
			isPlayerPushingJumpButton = false;
		} else {
			if (this.getControllingPassenger() instanceof EntityPlayer) {
				EntityPlayer entityPlayer = (EntityPlayer) this.getControllingPassenger();
				if (entityPlayer.capabilities.isCreativeMode) {
					currentFuelLevel = maxFuelLevel;
				}
			}

		}

		lastPosX = posX;
		lastPosY = posY;
		lastPosZ = posZ;
		lastAttribute1 = Attribute1;
		lastYaw = yaw;
		lastCurrentFuelLevel = currentFuelLevel;
		if (posY < 0) {
			this.setDead();
		}

		if (worldObj.isAirBlock(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))) || worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))).getBlock().getMaterial(worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d)))) == Material.WATER
				|| worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))).getBlock().getMaterial(worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d)))) == Material.LAVA || worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))).getBlock() == Blocks.SNOW_LAYER
				|| worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))).getBlock().getMaterial(worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d)))) == Material.PLANTS
				|| worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))).getBlock().getMaterial(worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d)))).isReplaceable()) {
			// in air block so fall i'll actually park the entity inside the
			// block below just a little bit.

			if (willSink) {
				this.motionY -= 0.03999999910593033D;
			} else {
				if (worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d))).getBlock().getMaterial(worldObj.getBlockState(new BlockPos((int) (posX - .5d), (int) posY, (int) (posZ - .5d)))) == Material.WATER || canFly) {
					// do nothing
					this.motionY = this.motionY * .85D;

				} else {
					this.motionY -= 0.03999999910593033D;
				}
			}

		} else

		{

			this.motionY = 0;
			this.posY = (int) this.posY + 1;
		}

		// end New for gravity

		if (getControllingPassenger() != null && currentFuelLevel > 0)

		{

			if (isPlayerAccelerating) {
				this.velocity += accelerationAmount;
			}
			if (isPlayerBreaking) {
				this.velocity -= accelerationAmount;
			}
			if (isPlayerTurningRight) {
				yaw += turnRate;
			}
			if (isPlayerTurningLeft) {
				yaw -= turnRate;
			}

		}
		if (isPlayerPushingJumpButton && currentFuelLevel > 0)

		{
			Attribute1 -= 1;
			if (Attribute1 < getMinAngle()) {
				Attribute1 = getMinAngle();
			}
		} else if (isPlayerPushingSprintButton && currentFuelLevel > 0)

		{
			Attribute1 += 1;
			if (Attribute1 > getMaxAngle()) {
				Attribute1 = getMaxAngle();
			}
		}
		// end take user input

		// Clamp values to max / min values as needed
		if (this.velocity > this.getMaxVelocity())

		{
			this.velocity = this.getMaxVelocity();
		} else if (this.velocity < this.getMaxVelocity() * -1)

		{
			this.velocity = this.getMaxVelocity() * -1;
		}
		if (this.velocity < 0.0001d && this.velocity > 0.0d)

		{
			this.velocity = 0d;

		} else if (this.velocity > -0.0001d && this.velocity < 0.0d)

		{
			this.velocity = 0d;
		}
		if (this.yaw > 360)

		{
			this.yaw = this.yaw - 360;
		} else if (this.yaw < 0)

		{
			this.yaw = 360 - this.yaw;
		}
		// END Clamp values to max / min values as needed

		// calc x & Z offsets needed for the given rotation & velocity
		double speedX = (velocity * MathHelper.cos((float) ((yaw + 90) * Math.PI / 180.0D)));
		double speedZ = (velocity * MathHelper.sin((float) ((yaw + 90) * Math.PI / 180.0D)));
		// double speedY=0;

		motionX = speedX;
		motionZ = speedZ;
		this.velocity *= .90;// apply friction

		setRotation(this.yaw, this.rotationPitch);

		// motionY= speedY;
		// setPosition( posX+speedX,posY+motionY, posZ+speedZ);
		if (this.worldObj.getBlockState(new BlockPos(this.posX, this.posY - 1, this.posZ)).getBlock() == ModBlocks.machinecompressedasphalt) {
			this.onGround = true;
		}

		moveEntity(motionX, motionY, motionZ);
		//
		// if (lastPosX != posX || lastPosY != posY || lastPosZ != posZ ||
		// lastAttribute1 != Attribute1 || sendInterval > 9) {
		// // only send the packet if something has changed should reduce
		// // network traffic if the entity is idle(just sitting still)
		// LogHelper.info("SendPacket");

		if (this.velocity != 0 || isPlayerTurningLeft || isPlayerTurningRight || isPlayerPushingJumpButton || isPlayerPushingSprintButton) {
			// machine is doing an activity
			runTimeTillNextFuelUsage = runTimeTillNextFuelUsage - 1;
			if (runTimeTillNextFuelUsage < 0) {
				runTimeTillNextFuelUsage = maxRunTimeTillNextFuelUsage;
				if (currentFuelLevel > 0) {
					currentFuelLevel--;// use fuel
				}
			}
		}

		// if (tickssincelastbroadcast > 20 || lastPosX != posX || lastPosY != posY || lastPosZ != posZ || lastAttribute1 != Attribute1 || lastYaw != yaw || lastCurrentFuelLevel != currentFuelLevel) {
		// something changed (or its been 1 second) so send it to clients in need
		ModNetwork.sendPacketToAllAround((new MachineModMessageEntityToClient(this.getEntityId(), this.posX, this.posY, this.posZ, this.yaw, this.Attribute1, this.Attribute2, this.currentFuelLevel)), new TargetPoint(worldObj.provider.getDimension(), posX, posY, posZ, 224)); // sendInterval = 0;
		// tickssincelastbroadcast = 0;
		// }
		// tickssincelastbroadcast = tickssincelastbroadcast + 1;
		// }
		//
		// sendInterval++;
		// ModNetwork.simpleNetworkWrapper.sendToAllAround((new
		// MachineModMessageEntityToClient(
		// this.getEntityId(),this.posX,this.posY,this.posZ,this.yaw,this.Attribute1)),
		// new TargetPoint(worldObj.provider.getDimensionId(), posX, posY, posZ,
		// 80));

		List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox());
		for (int i = 0; i < list.size(); ++i) {
			Entity entity = (Entity) list.get(i);
			if (entity != null) {
				if (entity instanceof EntityLivingBase) {
					if (!entity.isDead) {
						if (entity != this.getControllingPassenger()) {
							if (this.getControllingPassenger() != null) {
								// its alive & its not the rider & has a driver (prevents player exiting the machine from getting damaged)

								EntityLivingBase eLB = (EntityLivingBase) entity;
								eLB.attackEntityFrom(new DamageSource(randomDethMessage()), 5);
								// special case creepers because Evil !
								if (eLB instanceof EntityCreeper) {
									EntityCreeper eC = (EntityCreeper) eLB;
									// state 1 = ignited!
									eC.setCreeperState(1);
								}
							}
						}
					}
				}
			}
		}

	}

	public String randomDethMessage() {

		return Reference.MOD_ID + ":" + "GENERIC_CRUSH_MACHINE" + (this.rand.nextInt(5) + 1);
	}

	public void updateClient() {
		clientTicksSinceLastServerPulse++;
		if (ticksSinceLastParticle > nextParticleAtTick) {
			doParticleEffects();
			ticksSinceLastParticle = 0;
		}
		ticksSinceLastParticle++;

		// updateServer();
		// play the sound
		// worldObj.playSoundAtEntity(this, "engine", 1f, 1f);

		// this.noClip = true;
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		//
		// if(TargetYaw!=yaw){
		// YawTickCount=3;
		// }
		//

		this.motionX = (this.TargetposX - this.posX) / (3);
		this.motionY = (this.TargetposY - this.posY) / (3);
		this.motionZ = (this.TargetposZ - this.posZ) / (3);
		if (this.motionX > 1 || this.motionY > 1 || this.motionZ > 1) {
			// in cases of desync override the smoothing effect and just put the
			// entity in place
			// this resolves the issue of the entity jumping after placed.
			this.motionX *= 3;
			this.motionY *= 3;
			this.motionZ *= 3;
		}

		setPosition(posX + motionX, posY + motionY, posZ + motionZ);
		if (!this.isDead) {
			// only do for entities not dead so we dont keep them around on the client side.
			// this.addedToChunk = false;
			// 1.8.9 cant do this anymore causing major Rendering FPS issues.
		}
		// LogHelper.info("Client: isinvis:" +
		// this.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) +
		// " DIMID:" + worldObj.provider.getDimensionId() + " X:" + posX +
		// " Y:" + posY + " Z:" + posZ);
		//
		//
		// if(YawTickCount>0){
		//
		// this.rotationYaw += (TargetYaw -yaw)/YawTickCount;
		// YawTickCount--;
		// }
		// if (YawTickCount==0){
		// this.rotationYaw=TargetYaw;
		// }

		if (isFristTick) {
			clientInit();
		} else {
			isFristTick = false;
		}
		if (clientTicksSinceLastServerPulse > Reference.clientRemoveInactiveEntityTimer) {
			this.setDead();
			// this.worldObj.removeEntity(this);
			// this.worldObj.getChunkFromChunkCoords(this.chunkCoordX, this.chunkCoordZ).removeEntity(this);
			// this.addedToChunk = true;

		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	// override the set position and rotation function to avoid MC from setting
	// the postion of the entity so i can handle it
	// in my network handler ... avoids jitter
	public void setPositionAndRotationDirect(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_, boolean bool) {

	}

	// @SideOnly(Side.CLIENT)
	// public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_,
	// double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_) {
	//
	// }
	//
	// @SideOnly(Side.CLIENT)
	// public void setPositionAndRotation(double p_70056_1_, double p_70056_3_,
	// double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_) {
	//
	// }
	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!worldObj.isRemote) {
			// server side
			updateServer();

		} else {
			// client

			updateClient();
		}

	}

	// Probably not needed any more Replaced by updatePassenger
	public void updateRiderPosition() {
		if (this.getControllingPassenger() != null) {
			double d0 = Math.cos((double) this.rotationYaw * Math.PI / 180.0D) * this.velocity;
			double d1 = Math.sin((double) this.rotationYaw * Math.PI / 180.0D) * this.velocity;
			this.getControllingPassenger().setPosition(this.posX + d0 + this.getMountedXOffset(), this.posY + this.getMountedYOffset() + this.getControllingPassenger().getYOffset(), this.posZ + d1 + this.getMountedZOffset());
			// this.riddenByEntity.setRotationYawHead(this.yaw);
		}
	}

	@Override
	public void updatePassenger(Entity passenger) {
		if (this.isPassenger(passenger)) {

			if (this.getControllingPassenger() != null) {
				double d0 = Math.cos((double) this.rotationYaw * Math.PI / 180.0D) * this.velocity;
				double d1 = Math.sin((double) this.rotationYaw * Math.PI / 180.0D) * this.velocity;
				passenger.setPosition(this.posX + d0 + this.getMountedXOffset(), this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + d1 + this.getMountedZOffset());
				// this.riddenByEntity.setRotationYawHead(this.yaw);
			}
		}
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub

		// super.readFromNBT(compound);
		// no need to call super it calls this method instead.

		yaw = compound.getFloat(Reference.MACHINE_MOD_NBT_PREFIX + "YAW");
		velocity = compound.getDouble(Reference.MACHINE_MOD_NBT_PREFIX + "VELOCITY");
		TargetposX = compound.getDouble(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_X");
		TargetposY = compound.getDouble(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_Y");
		TargetposZ = compound.getDouble(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_Z");
		TargetYaw = compound.getFloat(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_YAW");
		Attribute1 = compound.getFloat(Reference.MACHINE_MOD_NBT_PREFIX + "ATTRIBUTE1");
		currentFuelLevel = compound.getInteger(Reference.MACHINE_MOD_NBT_PREFIX + "CURRENT_FUEL");
		runTimeTillNextFuelUsage = compound.getInteger(Reference.MACHINE_MOD_NBT_PREFIX + "RUN_TIMER_EMAIN");
		// inventory
		NBTTagList tagList = compound.getTagList(Reference.MACHINE_MOD_NBT_PREFIX + "Inventory", compound.getId());
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		// super.writeToNBT(compound);
		// no need to call the super it calls this method instead
		compound.setFloat(Reference.MACHINE_MOD_NBT_PREFIX + "YAW", yaw);
		compound.setDouble(Reference.MACHINE_MOD_NBT_PREFIX + "VELOCITY", velocity);
		compound.setDouble(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_X", TargetposX);
		compound.setDouble(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_Y", TargetposY);
		compound.setDouble(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_Z", TargetposZ);
		compound.setFloat(Reference.MACHINE_MOD_NBT_PREFIX + "TARGET_YAW", TargetYaw);
		compound.setFloat(Reference.MACHINE_MOD_NBT_PREFIX + "ATTRIBUTE1", Attribute1);
		compound.setInteger(Reference.MACHINE_MOD_NBT_PREFIX + "CURRENT_FUEL", currentFuelLevel);
		compound.setInteger(Reference.MACHINE_MOD_NBT_PREFIX + "RUN_TIMER_EMAIN", runTimeTillNextFuelUsage);
		// inventory
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = inventory[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		compound.setTag(Reference.MACHINE_MOD_NBT_PREFIX + "Inventory", itemList);

	}

	public float getMaxAngle() {
		return this.maxAngle;
	}

	public float getMinAngle() {
		return this.minAngle;
	}

	public double calcOffsetX(double distance) {
		return (distance * MathHelper.cos((float) (clampAngelto360(this.yaw + 90) * Math.PI / 180.0D)));
	}

	public double calcOffsetZ(double distance) {
		return (distance * MathHelper.sin((float) (clampAngelto360(this.yaw + 90) * Math.PI / 180.0D)));

	}

	public double calcOffsetX(double distance, float rot) {
		return (distance * MathHelper.cos((float) (clampAngelto360(rot + 90f) * Math.PI / 180.0D)));
	}

	public double calcOffsetZ(double distance, float rot) {
		return (distance * MathHelper.sin((float) (clampAngelto360(rot + 90f) * Math.PI / 180.0D)));

	}

	public BlockPos calculateBlockPosGivenStartAngleDistance4(double startX, double startY, double startZ, float yaw1, float pitch1, double distance1, float yaw2, float pitch2, double distance2, float yaw3, float pitch3, double distance3, float yaw4, float pitch4, double distance4) {

		// Campled angles in radians
		float clampYaw1 = (float) (clampAngelto360(yaw1) * Math.PI / 180D);
		float clampPitchZ1 = (float) (clampAngelto360(pitch1) * Math.PI / 180D);
		float clampYaw2 = (float) (clampAngelto360(yaw2) * Math.PI / 180D);
		float clampPitch2 = (float) (clampAngelto360(pitch2) * Math.PI / 180D);
		float clampYaw3 = (float) (clampAngelto360(yaw3) * Math.PI / 180D);
		float clampPitch3 = (float) (clampAngelto360(pitch3) * Math.PI / 180D);
		float clampYaw4 = (float) (clampAngelto360(yaw4) * Math.PI / 180D);
		float clampPitch4 = (float) (clampAngelto360(pitch4) * Math.PI / 180D);

		// float clampYaw5 = (float) (clampAngelto360(yaw5) * Math.PI / 180D);
		// float clampPitch5 = (float) (clampAngelto360(pitch5) * Math.PI / 180D);

		//
		startX = startX + distance1 * MathHelper.cos(clampPitchZ1) * MathHelper.sin(clampYaw1);
		startY = startY + distance1 * MathHelper.sin(clampPitchZ1);
		startZ = startZ + distance1 * MathHelper.cos(clampPitchZ1) * MathHelper.cos(clampYaw1);

		// startX = startX + distance1 * MathHelper.sin(ClampY1); MathHelper.sin(ClampY1);
		// startY = startY + distance1 * MathHelper.sin(ClampZ1);
		// startZ = startZ + distance1 * MathHelper.cos(ClampY1);

		startX = startX + distance2 * MathHelper.cos(clampPitch2) * MathHelper.sin(clampYaw2);
		startY = startY + distance2 * MathHelper.sin(clampPitch2);
		startZ = startZ + distance2 * MathHelper.cos(clampPitch2) * MathHelper.cos(clampYaw2);

		startX = startX + distance3 * MathHelper.cos(clampPitch3) * MathHelper.sin(clampYaw3);
		startY = startY + distance3 * MathHelper.sin(clampPitch3);
		startZ = startZ + distance3 * MathHelper.cos(clampPitch3) * MathHelper.cos(clampYaw3);

		startX = startX + distance4 * MathHelper.cos(clampPitch4) * MathHelper.sin(clampYaw4);
		startY = startY + distance4 * MathHelper.sin(clampPitch4);
		startZ = startZ + distance4 * MathHelper.cos(clampPitch4) * MathHelper.cos(clampYaw4);

		// startX = startX + distance5 * MathHelper.cos(clampPitch5) * MathHelper.sin(clampYaw5);
		// startY = startY + distance5 * MathHelper.sin(clampPitch5);
		// startZ = startZ + distance5 * MathHelper.cos(clampPitch5) * MathHelper.cos(clampYaw5);

		return new BlockPos(startX, startY, startZ);
	}

	public double calcTwoOffsetX(double distance, int secondOffsetAngle, double secondOffsetDistance) {

		if (secondOffsetAngle == 0) {
			return (calcOffsetX(distance));
		}
		// calc first xPos
		double firstX = calcOffsetX(distance);

		return firstX + (secondOffsetDistance * MathHelper.cos((float) (clampAngelto360(this.yaw + secondOffsetAngle + 90) * Math.PI / 180.0D)));
	}

	public double calcTwoOffsetZ(double distance, int secondOffsetAngle, double secondOffsetDistance) {

		if (secondOffsetAngle == 0) {
			return (calcOffsetZ(distance));
		}
		// calc first xPos
		double firstZ = calcOffsetZ(distance);

		return firstZ + (secondOffsetDistance * MathHelper.sin((float) (clampAngelto360(this.yaw + secondOffsetAngle + 90) * Math.PI / 180.0D)));
	}

	public float clampAngelto360(float inAngle) {

		while (inAngle > 360) {
			inAngle -= 360;
		}

		while (inAngle < 0) {
			inAngle = 360 - inAngle;
		}
		return inAngle;
	}

	// && (previousBlock == Blocks.log || previousBlock == Blocks.log2)
	// && (previousBlock == Blocks.log || previousBlock == Blocks.log2)
	public void toppleTree(BlockPos bp, int depth, int widthDepth, Block previousBlock) {
		if (depth < Reference.MAX_TREE_DEPTH) {
			if (widthDepth < Reference.MAX_TREE_WIDTH) {
				if (worldObj.getBlockState(bp).getBlock() == Blocks.LOG || worldObj.getBlockState(bp).getBlock() == Blocks.LOG2 || worldObj.getBlockState(bp).getBlock() == Blocks.LEAVES || worldObj.getBlockState(bp).getBlock() == Blocks.LEAVES2 || worldObj.getBlockState(bp).getBlock().isWood(worldObj, bp)
						|| worldObj.getBlockState(bp).getBlock().isLeaves(worldObj.getBlockState(bp), worldObj, bp)) {

					previousBlock = worldObj.getBlockState(bp).getBlock();
					BlockUtil.BreakBlock(worldObj, bp, this.getControllingPassenger());

					toppleTree(bp.offset(EnumFacing.DOWN), depth + 1, widthDepth, previousBlock);
					toppleTree(bp.offset(EnumFacing.UP), depth + 1, widthDepth, previousBlock);
					toppleTree(bp.offset(EnumFacing.SOUTH), depth + 1, widthDepth + 1, previousBlock);
					toppleTree(bp.offset(EnumFacing.EAST), depth + 1, widthDepth + 1, previousBlock);
					toppleTree(bp.offset(EnumFacing.WEST), depth + 1, widthDepth + 1, previousBlock);
					toppleTree(bp.offset(EnumFacing.NORTH), depth + 1, widthDepth + 1, previousBlock);

				}
			}
		}
	}

	// adds to this objects inventory if it can
	// any remaining amount will be returned
	protected ItemStack addToinventory(ItemStack is) {
		int i = getSizeInventory();

		for (int j = 0; j < i && is != null && is.stackSize > 0; ++j) {
			if (is != null) {

				if (getStackInSlot(j) != null) {
					if (getStackInSlot(j).getItem() == is.getItem() && getStackInSlot(j).getItemDamage() == is.getItemDamage()) {
						// same item remove from is put into slot any amt not to
						// excede stack max
						if (getStackInSlot(j).stackSize < getStackInSlot(j).getMaxStackSize()) {
							// we have room to add to this stack
							if (is.stackSize <= getStackInSlot(j).getMaxStackSize() - getStackInSlot(j).stackSize) {
								// /all of the stack will fit in this slot do
								// so.

								setInventorySlotContents(j, new ItemStack(getStackInSlot(j).getItem(), getStackInSlot(j).stackSize + is.stackSize, is.getItemDamage()));
								is = null;
							} else {
								// we have more
								int countRemain = is.stackSize - (getStackInSlot(j).getMaxStackSize() - getStackInSlot(j).stackSize);
								setInventorySlotContents(j, new ItemStack(is.getItem(), getStackInSlot(j).getMaxStackSize(), is.getItemDamage()));
								is.stackSize = countRemain;
							}

						}
					}
				} else {
					// nothign in slot so set contents
					setInventorySlotContents(j, is.copy());
					is = null;
				}

			}

		}
		// bug fix for picking up items that cannot be put in inventory
		return is;

	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory[slot];
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		if (!(this.worldObj.isRemote)) {
			// send packet to notify client of contents of machine's inventory
			if (this.shouldSendClientInvetoryUpdates) {
				ModNetwork.sendPacketToAllAround((new MachineModMessageEntityInventoryChangedToClient(this.getEntityId(), slot, inventory[slot])), new TargetPoint(worldObj.provider.getDimension(), posX, posY, posZ, 80));
			}
		}

	}

	public void sendAllInventoryToPlayer(EntityPlayerMP player) {
		for (int i = 0; i < inventory.length; i++) {

			ModNetwork.simpleNetworkWrapper.sendTo(new MachineModMessageEntityInventoryChangedToClient(this.getEntityId(), i, inventory[i]), player);

		}

	}

	@Override
	public void markDirty() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openInventory(EntityPlayer playerIn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeInventory(EntityPlayer playerIn) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		// check if the player is near the entity.
		return player.getDistanceSq(posX, posY, posZ) < 64;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= amt) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}

			}
		}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			setInventorySlotContents(slot, null);
		}
		return stack;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < inventory.length; ++i) {
			inventory[i] = null;
		}
	}

	/**
	 * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example, Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
	 */
	@Override
	@Nullable
	public Entity getControllingPassenger() {
		List<Entity> list = this.getPassengers();
		return list.isEmpty() ? null : (Entity) list.get(0);
	}

}
