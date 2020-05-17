package pokmon987.hammerandvil.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokmon987.hammerandvil.HAVConfig;
import pokmon987.hammerandvil.HammerAndVil;
import pokmon987.hammerandvil.recipes.VilRecipes;
import pokmon987.hammerandvil.tileentity.TileVil;
import pokmon987.hammerandvil.tileentity.TileVil.LastHitTool;
import pokmon987.hammerandvil.util.EqualCheck;
import pokmon987.hammerandvil.util.HitHandler;

public class BlockVil extends Block {
	
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	protected static final AxisAlignedBB X_AXIS_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.09D, 1.0D, 0.875D, 0.91D);
	protected static final AxisAlignedBB Z_AXIS_AABB = new AxisAlignedBB(0.09D, 0.0D, 0.0D, 0.91D, 0.875D, 1.0D);
	
	public BlockVil() {
		super(Material.IRON);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setHardness(3.0F);
		setRegistryName(new ResourceLocation(HammerAndVil.MODID, "vil"));
		setUnlocalizedName(HammerAndVil.MODID + ".vil");
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing facing) {
		return BlockFaceShape.UNDEFINED;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing());
		
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
		
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileVil tile = (TileVil)worldIn.getTileEntity(pos);
		NonNullList<ItemStack> inv = tile.getAllStacks();
		if (inv == null) {return;}
		inv.forEach(item -> {
			EntityItem entity = new EntityItem(worldIn, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D,item);
			worldIn.spawnEntity(entity);
		});
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing enumfacing = state.getValue(FACING);
		return enumfacing.getAxis() == EnumFacing.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
		
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			ItemStack handItem = playerIn.getHeldItemOffhand().isEmpty() ? playerIn.getHeldItemMainhand() : playerIn.getHeldItemOffhand();
			TileVil tile = (TileVil)worldIn.getTileEntity(pos);
			int maxItems = HAVConfig.General.multipleItemsInStack ? tile.getInventory().getSlotLimit(0) : 1;
			if (tile != null) {
				ItemStack stack = tile.getInventory().getStackInSlot(0);
				// if stack is less that the max slot size and hand is equal to the current stack
				if (stack.getCount() < maxItems && tile.getInventory().getStackInSlot(1).isEmpty() && !handItem.isEmpty() && ((EqualCheck.areEqual(handItem, stack) && ItemStack.areItemStackTagsEqual(stack, handItem)) || stack.isEmpty()) && stack.getMaxStackSize() > 1) {
					ItemStack itemInv = handItem.copy();
					itemInv.setCount(1);
					// increase count by one
					tile.getInventory().insertItem(0, itemInv, false);
					tile.hits.resetHits();
					handItem.setCount(handItem.getCount()-1);
					worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
					worldIn.notifyBlockUpdate(pos, state, state, 1);
					// else if player is also holding shift/crouching, remove the whole stack and spawn it in the world
				} else if (stack.getCount() == 1 && tile.getInventory().getStackInSlot(1).isEmpty() && !handItem.isEmpty() && HAVConfig.General.multipleSlots) {
					ItemStack itemInv = handItem.copy();
					itemInv.setCount(1);
					tile.getInventory().insertItem(1, itemInv, false);
					tile.hits.resetHits();
					handItem.setCount(handItem.getCount()-1);
					worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
					worldIn.notifyBlockUpdate(pos, state, state, 1);
				} else if (!tile.getInventory().getStackInSlot(1).isEmpty() && tile.getInventory().getStackInSlot(2).isEmpty() && !handItem.isEmpty() && HAVConfig.General.multipleSlots) {
					ItemStack itemInv = handItem.copy();
					itemInv.setCount(1);
					tile.getInventory().insertItem(2, itemInv, false);
					tile.hits.resetHits();
					handItem.setCount(handItem.getCount()-1);
					worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 1.0F);
					worldIn.notifyBlockUpdate(pos, state, state, 1);
				} else if (!stack.isEmpty()) {
					EntityItem entity = new EntityItem(worldIn, pos.getX()+0.5D, pos.getY()+1D, pos.getZ()+0.5D, stack.copy());
					entity.motionX = 0;
					entity.motionY = 0;
					entity.motionZ = 0;
					worldIn.spawnEntity(entity);
					tile.getInventory().setStackInSlot(0, ItemStack.EMPTY);
					if (!tile.getInventory().getStackInSlot(1).isEmpty()) {
						EntityItem entity2 = new EntityItem(worldIn, pos.getX()+0.5D, pos.getY()+1D, pos.getZ()+0.5D, tile.getInventory().getStackInSlot(1).copy());
						entity2.motionX = 0;
						entity2.motionY = 0;
						entity2.motionZ = 0;
						tile.getInventory().setStackInSlot(1, ItemStack.EMPTY);
						worldIn.spawnEntity(entity2);
					}
					if (!tile.getInventory().getStackInSlot(2).isEmpty()) {
						EntityItem entity3 = new EntityItem(worldIn, pos.getX()+0.5D, pos.getY()+1D, pos.getZ()+0.5D, tile.getInventory().getStackInSlot(2).copy());
						entity3.motionX = 0;
						entity3.motionY = 0;
						entity3.motionZ = 0;
						tile.getInventory().setStackInSlot(2, ItemStack.EMPTY);
						worldIn.spawnEntity(entity3);
					}
					tile.hits.resetHits();
					worldIn.notifyBlockUpdate(pos, state, state, 1);
				}
				tile.markDirty();
			}
		}
		return true;
	}
	
	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
		if (!worldIn.isRemote) {
			
			if (this.blockHardness == -1.0F) {this.setHardness(3.0F);}
			
			ItemStack hand = playerIn.getHeldItemMainhand();
			TileVil tile = (TileVil)worldIn.getTileEntity(pos);
			HitHandler hits = tile.hits;
			LastHitTool lastTool = tile.getToolHandler();
			hits.setCurrentHits(lastTool.get() == null || EqualCheck.areEqual(hand, lastTool.get()) ? hits.getCurrentHits() : hits.resetHits());
			NonNullList<ItemStack> stacks = tile.getAllStacks();
			String recipeName = VilRecipes.getNameForRecipe(stacks, hand);
			if (!VilRecipes.getVilResultByName(recipeName).isEmpty() && !VilRecipes.getToolForRecipe(hand, stacks).isEmpty()) {
				ItemStack requiredTool = VilRecipes.getToolForRecipe(hand, stacks);
				if (hand.getItem() == requiredTool.getItem() && EqualCheck.areEqual(hand, requiredTool)) {
					setBlockUnbreakable();
					lastTool.set(hand.copy());
					Float requiredHits = VilRecipes.getHitsRequiredByName(recipeName);
					//If it's not empty, the hits variable increases by 1.0F
					hits.increaseHitUntilPoint(requiredHits);
					//System.out.println("The result was not empty! The number of hits is at: " + hits.getCurrentHits());
					worldIn.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.3F, 1.5F);
					//If the hits is equal to the current stack's requirement, it replaces the item with the result
					if (hits.getCurrentHits() >= requiredHits) {
						if (!HAVConfig.General.dropOnCraft) {
							tile.getInventory().setStackInSlot(0, VilRecipes.getVilResultByName(recipeName));
							tile.getInventory().setStackInSlot(1, ItemStack.EMPTY);
							tile.getInventory().setStackInSlot(2, ItemStack.EMPTY);
						} else {
							tile.setAllStacksEmpty();
							EntityItem entity = new EntityItem(worldIn, pos.getX()+0.5D, pos.getY()+1D, pos.getZ()+0.5D, VilRecipes.getVilResultByName(recipeName));
							entity.motionX = 0;
							entity.motionY = 0;
							entity.motionZ = 0;
							worldIn.spawnEntity(entity);
						}
						worldIn.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.8F, 1.9F);
						worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos), 1);
						hits.resetHits();
						//add config option here to if damage item is onCraft then damage
						if (hand.getMaxDamage() > 0) {
							//if the hand item has a maxdamage that is over 0, damage the item by 1
							hand.damageItem(1, playerIn);
						} else if (hand.serializeNBT().hasKey("tag")) {
							//if the hand item has an NBT key that is "tag", then it will damage the item by two points
							//this is for Immersive Engineering tools, because of their tools being setup with subtypes
							int damageAmount = hand.serializeNBT().getCompoundTag("tag").getInteger("Damage");
							NBTTagCompound tag = new NBTTagCompound();
							hand.writeToNBT(tag);
							tag.getCompoundTag("tag").setInteger("Damage", damageAmount + 2);
							hand.serializeNBT().merge(tag);
						} else {
							// if nothing else is true, then shrink the itemstack size by 1
							hand.shrink(1);
						}
					}
					if (playerIn.isCreative()) {
						//This area was for setting up a way for the block to not be broken
						
					}
				}
			}
		}
	}
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.playSound(pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1, 1, false);
		super.onBlockAdded(worldIn, pos, state);
	}
	
	
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileVil();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING});
	}
}