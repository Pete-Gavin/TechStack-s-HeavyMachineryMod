package com.projectreddog.machinemod.item.machines;

import com.projectreddog.machinemod.entity.EntityLawnmower;
import com.projectreddog.machinemod.entity.EntityMachineModRideable;
import com.projectreddog.machinemod.model.ModelLawnmower;
import com.projectreddog.machinemod.model.ModelTransportable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLawnmower extends ItemTransportable {

	public ModelTransportable mt;

	public ItemLawnmower() {
		super();
		this.setUnlocalizedName("lawnmower");
		this.maxStackSize = 1;

	}

	@Override
	// public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World
	// worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float
	// hitZ)
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOff, float yOff, float zOff) {
		boolean result = false;

		if (!world.isRemote)// / only run on server
		{
			// LogHelper.info("Item used on Bulldozer!");
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			EntityMachineModRideable entityLawnmower = getEntityToSpawn(world);
			entityLawnmower.setPosition(x + .5d, y + 1.0d, z + .5d);
			entityLawnmower.prevPosX = x + .5d;
			entityLawnmower.prevPosY = y + 1.0d;
			entityLawnmower.prevPosZ = z + .5d;
			result = world.spawnEntityInWorld(entityLawnmower);
			// LogHelper.info("Spawn entity resutl:" + result );
			if (result && !player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
		}
		if (result) {
			return EnumActionResult.PASS;
		} else {
			return EnumActionResult.FAIL;
		}
	}

	public EntityMachineModRideable getEntityToSpawn(World world) {
		return new EntityLawnmower(world);

	}

	@Override
	public ModelTransportable getModel() {
		if (mt == null) {
			mt = new ModelLawnmower();
		}
		return mt;
	}
}
