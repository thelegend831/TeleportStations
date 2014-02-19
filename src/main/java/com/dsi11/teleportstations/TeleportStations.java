package com.dsi11.teleportstations;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import com.dsi11.teleportstations.blocks.BlockTeleMid;
import com.dsi11.teleportstations.blocks.BlockTeleTarget;
import com.dsi11.teleportstations.blocks.BlockTeleTop;
import com.dsi11.teleportstations.blocks.BlockTeleporter;
import com.dsi11.teleportstations.database.TPDatabase;
import com.dsi11.teleportstations.database.TPFileHandler;
import com.dsi11.teleportstations.entities.EntitySpawnPearl;
import com.dsi11.teleportstations.entities.TileEntityTeleporter;
import com.dsi11.teleportstations.items.ItemSpawnPearl;
import com.dsi11.teleportstations.items.ItemTeleporter;
import com.dsi11.teleportstations.packethandler.TPPacketHandler;
import com.dsi11.teleportstations.packethandler.TPPlayerTracker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

/**
 * Mainclass of Minecraft Mod Teleport Stations.
 * <p>
 * Registers blocks and all other needed modules.
 * 
 * @author Adanaran
 * @author Demitreus
 */
@Mod(modid = "TeleportStations", name = "Teleport Stations", version = "0.1")
@NetworkMod(channels = { "tpChange", "tpRemove", "tpDB" }, versionBounds = "[0.1,)", clientSideRequired = true, serverSideRequired = false, packetHandler = TPPacketHandler.class)
public class TeleportStations {

	// The mod instance
	@Instance
	public static TeleportStations instance;
	// The sided Proxy instance
	@SidedProxy(clientSide = "com.dsi11.teleportstations.ClientProxy", serverSide = "com.dsi11.teleportstations.CommonProxy")
	public static CommonProxy proxy;
	// The database
	public static TPDatabase db;
	// The filehandler
	public static TPFileHandler fh;
	// The playertracker
	public static TPPlayerTracker pt;
	// The blocks
	public static BlockTeleTarget blockTeleTarget;
	public static BlockTeleporter blockTeleporter;
	public static BlockTeleporter blockTeleporterAn;
	public static BlockTeleMid blockTeleMid;
	public static BlockTeleTop blockTeleTop;
	// The items
	public static ItemTeleporter itemTele;
	public static ItemSpawnPearl itemSpawnPearl;
	// The Minecraft dir
	public static String dir;
	// The Logger
	public static Logger logger;
	// The IDs
	private int idBlockTeleTarget;
	private int idBlockTeleporter;
	private int idBlockTeleporterAn;
	private int idBlockTeleMid;
	private int idBlockTeleTop;
	private int idHandtele;
	private int idSpawnPearl;

	/**
	 * The pre-initialization method.
	 * <p>
	 * Registers the (client-)GUI-handler.
	 * 
	 * @param event
	 *            FMLPreInitializationEvent
	 */
	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		dir = event.getModConfigurationDirectory().getPath()
				.replace("config", "");
		logger = event.getModLog();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		Configuration cfg = new Configuration(
				event.getSuggestedConfigurationFile());
		try {
			cfg.load();
			idBlockTeleTarget = cfg.getBlock("blockteletarget", 3001).getInt(
					3001);
			idBlockTeleporter = cfg.getBlock("blockteleporter", 3002).getInt(
					3002);
			idBlockTeleporterAn = cfg.getBlock("blockteleporteron", 3003)
					.getInt(3003);
			idBlockTeleMid = cfg.getBlock("blocktelemid", 3004).getInt(3004);
			idBlockTeleTop = cfg.getBlock("blockteletop", 3005).getInt(3005);
			idHandtele = cfg.getBlock("mobileteleporter", 3006).getInt(3006);
			idSpawnPearl = cfg.getBlock("spawmpearl", 3007).getInt(3007);
			logger.log(Level.INFO, "TeleportStations configuration loaded.");
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"Failed loading TeleportStations configuration", e);
		} finally {
			cfg.save();
		}
	}

	/**
	 * The load method.
	 * <p>
	 * Registers the blocks and initializes other modules.
	 * 
	 * @param evt
	 *            FMLInitializationEvent
	 */
	@Init
	public void load(FMLInitializationEvent evt) {
		logger.log(Level.FINE, "Registering blocks and items");
		registerBlockTeleTarget(idBlockTeleTarget);
		registerBlockTeleporter(idBlockTeleporter, idBlockTeleporterAn);
		registerBlockTeleMid(idBlockTeleMid);
		registerBlockTeleTop(idBlockTeleTop);
		registerSpawnPearl(idSpawnPearl);
		registerHandtele(idHandtele);
		proxy.registerRenderInformation();
		db = new TPDatabase();
		fh = new TPFileHandler(db);
		pt = new TPPlayerTracker(db, fh);
		GameRegistry.registerPlayerTracker(pt);
	}

	private void registerSpawnPearl(int i) {
		itemSpawnPearl = new ItemSpawnPearl(i);
		itemSpawnPearl.setCreativeTab(CreativeTabs.tabTransport).setItemName("Spawnpearl");
		EntityRegistry.registerModEntity(EntitySpawnPearl.class, "Spawnpearl",
				3, this, 164, 10, true);
		LanguageRegistry.addName(itemSpawnPearl, "Spawnpearl");
		GameRegistry.registerItem(itemSpawnPearl, itemSpawnPearl.getItemName());
		GameRegistry.addRecipe(
				new ItemStack(itemSpawnPearl),
				new Object[] { "K", "E", Character.valueOf('E'),
								Item.enderPearl, Character.valueOf('K'),
								Item.compass });
	}

	private void registerHandtele(int i) {
		itemTele = new ItemTeleporter(i);
		itemTele.setCreativeTab(CreativeTabs.tabTransport).setItemName("Handteleporter");
		LanguageRegistry.addName(itemTele, "Handteleporter");
		GameRegistry.registerItem(itemTele, itemTele.getItemName());
		GameRegistry.addRecipe(
				new ItemStack(itemTele),
				new Object[] { "EPE", "EKE","ETE", Character.valueOf('E'),
								Item.ingotIron, Character.valueOf('T'),
								blockTeleporter, Character.valueOf('P'),
								itemSpawnPearl, Character.valueOf('K'),
								Item.coal });
	}

	/**
	 * The post-init method.
	 * <p>
	 * Registers the LoadCallback-handler.
	 * 
	 * @param evt
	 *            FMLPostInitializationEvent
	 */
	@PostInit
	public void modsLoaded(FMLPostInitializationEvent evt) {
		logger.log(Level.FINE, "done loading");
	}

	private void registerBlockTeleTarget(int id) {
		blockTeleTarget = new BlockTeleTarget(id);
		blockTeleTarget.setBlockName("Teleporterziel");
		blockTeleTarget.setCreativeTab(CreativeTabs.tabTransport);
		LanguageRegistry.addName(blockTeleTarget, "Teleporterziel");
		LanguageRegistry.instance().addNameForObject(blockTeleTarget, "de_DE",
				"Teleporterziel");
		GameRegistry.registerBlock(blockTeleTarget,
				blockTeleTarget.getBlockName());

		GameRegistry.addRecipe(new ItemStack(blockTeleTarget), new Object[] {
				"DOD", "ORO", "DOD", Character.valueOf('D'), Block.glass,
				Character.valueOf('O'), Block.obsidian, Character.valueOf('R'),
				Item.redstone });
	}

	private void registerBlockTeleporter(int id, int idAn) {
		blockTeleporter = new BlockTeleporter(id);
		blockTeleporterAn = new BlockTeleporter(idAn);
		blockTeleporter.setBlockName("Teleporter");
		blockTeleporterAn.setBlockUnbreakable();
		blockTeleporter.setCreativeTab(CreativeTabs.tabTransport);
		GameRegistry.registerBlock(blockTeleporter,
				blockTeleporter.getBlockName());
		GameRegistry.registerBlock(blockTeleporterAn,
				blockTeleporterAn.getBlockName());
		LanguageRegistry.addName(blockTeleporter, "Teleporter");
		GameRegistry.addRecipe(new ItemStack(blockTeleporter), new Object[] {
				"DOD", "ORO", "DOD", Character.valueOf('D'), Item.diamond,
				Character.valueOf('O'), Block.obsidian, Character.valueOf('R'),
				Item.redstone });
	}

	private void registerBlockTeleTop(int id) {
		blockTeleTop = new BlockTeleTop(id);
		GameRegistry.registerBlock(blockTeleTop, "Teleporterdeckel");
		LanguageRegistry.addName(blockTeleTop, "Teleporterdeckel");
		blockTeleTop.setBlockUnbreakable().setBlockBounds(0.01f, 0.5f, 0.01f,
				0.99f, 1f, 0.99f);
		GameRegistry.registerTileEntity(TileEntityTeleporter.class,
				"TileEntityTeleporter");
	}

	private void registerBlockTeleMid(int id) {
		blockTeleMid = new BlockTeleMid(id);
		GameRegistry.registerBlock(blockTeleMid, "Teleportermitte");
		LanguageRegistry.addName(blockTeleMid, "Teleportermitte");
		blockTeleMid.setBlockUnbreakable().setBlockBounds(0.5f, 1.5F, 0.5f,
				0.5f, 1.5f, 0.5f);
	}
}