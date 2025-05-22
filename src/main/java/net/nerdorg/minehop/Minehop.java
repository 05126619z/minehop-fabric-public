package net.nerdorg.minehop;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.nerdorg.minehop.block.ModBlocks;
import net.nerdorg.minehop.block.entity.ModBlockEntities;
import net.nerdorg.minehop.commands.*;
import net.nerdorg.minehop.config.MinehopConfig;
import net.nerdorg.minehop.config.ConfigWrapper;
import net.nerdorg.minehop.data.DataManager;
import net.nerdorg.minehop.entity.MobManager;
import net.nerdorg.minehop.entity.ModEntities;
import net.nerdorg.minehop.entity.custom.ResetEntity;
import net.nerdorg.minehop.entity.custom.Zone;
import net.nerdorg.minehop.hns.HNSManager;
import net.nerdorg.minehop.item.ModItems;
import net.nerdorg.minehop.motd.MotdManager;
import net.nerdorg.minehop.networking.*;
import net.nerdorg.minehop.networking.payloads.*;
import net.nerdorg.minehop.replays.ReplayEvents;
import net.nerdorg.minehop.replays.ReplayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Minehop implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("minehop");
    public static final String MOD_ID = "minehop";
    public static final int MOD_VERSION = 1014;
    public static final String MOD_VERSION_STRING = "1.0.14";

	public static boolean override_config = false;
	public static double o_sv_friction = 0;
	public static double o_sv_accelerate = 0;
	public static double o_sv_airaccelerate = 0;
	public static double o_sv_maxairspeed = 0;
	public static double o_speed_mul = 0;
	public static double o_sv_gravity = 0;
	public static double o_speed_cap = 0;
	public static double o_speed_coefficient = 0;
	public static boolean o_hns = false;
	public static boolean o_enabled = true;
	public static boolean o_fall_damage = true;

	public static boolean receivedConfig = false;

	public static List<DataManager.MapData> mapList = new ArrayList<>();
	public static List<DataManager.RecordData> personalRecordList = new ArrayList<>();
	public static List<DataManager.RecordData> recordList = new ArrayList<>();
	public static List<ReplayManager.Replay> replayList = new ArrayList<>();

	public static List<String> groundedList = new ArrayList<>();
	public static HashMap<String, HashMap<String, Long>> timerManager = new HashMap<>();
	public static HashMap<String, Double> efficiencyMap = new HashMap<>();
	public static HashMap<String, List<Double>> efficiencyListMap = new HashMap<>();
	public static HashMap<String, ReplayManager.SSJEntry> lastEfficiencyMap = new HashMap<>();
	public static HashMap<String, Double> efficiencyUpdateMap = new HashMap<>();
	public static HashMap<String, Double> speedCapMap = new HashMap<>();
	public static HashMap<String, List<Double>> gaugeListMap = new HashMap<>();
	public static HashMap<String, Zone> playerMapLocation = new HashMap<>();

	public static List<PlayerEntity> currentCheaters = new ArrayList<>();

	@Override
	public void onInitialize() {
		AutoConfig.register(MinehopConfig.class, JanksonConfigSerializer::new);
		ConfigWrapper.loadConfig();

		PacketHandler.register();

		ServerPlayConnectionEvents.INIT.register(((serverPlayNetworkHandler, minecraftServer) -> {
			PacketHandler.registerReceivers();
			HandshakeHandler.register();
		}));

		ConfigWrapper.register();
		DataManager.register();
		JoinLeaveManager.register();
		MobManager.register();

		HNSManager.register();

		CommandRegister.register();

		ReplayManager.register();
		ReplayEvents.register();

		ModItems.initialize();
		ModItems.registerModItems();
		ModBlockEntities.registerBlockEntities();

		MotdManager.register();

		FabricDefaultAttributeRegistry.register(ModEntities.RESET_ENTITY, ResetEntity.createResetEntityAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.START_ENTITY, ResetEntity.createResetEntityAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.END_ENTITY, ResetEntity.createResetEntityAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.REPLAY_ENTITY, ResetEntity.createResetEntityAttributes());
	}
}