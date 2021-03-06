package com.projectreddog.machinemod.block;

import com.projectreddog.machinemod.creativetab.CreativeTabMachineMod;
import com.projectreddog.machinemod.reference.Reference;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockBioFuel extends BlockFluidClassic {

	public BlockBioFuel(Fluid fluid, Material material) {
		super(fluid, material);
		// TODO Auto-generated constructor stub
		setCreativeTab(CreativeTabMachineMod.MACHINEMOD_ITEMS_TAB);
		this.setUnlocalizedName(Reference.MOD_ID.toLowerCase() + ":" + Reference.MODBLOCK_MACHINE_FLUID_BIOFUEL);

	}

	@Override
	public boolean canDisplace(IBlockAccess world, BlockPos bp) {
		if (world.getBlockState(bp).getBlock().getMaterial(world.getBlockState(bp)).isLiquid())
			return false;
		return super.canDisplace(world, bp);
	}

	@Override
	public boolean displaceIfPossible(World world, BlockPos bp) {
		if (world.getBlockState(bp).getBlock().getMaterial(world.getBlockState(bp)).isLiquid())
			return false;
		return super.displaceIfPossible(world, bp);
	}

}
