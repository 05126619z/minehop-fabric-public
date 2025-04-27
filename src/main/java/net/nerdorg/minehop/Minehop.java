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
    public static final int MOD_VERSION = 1013;
    public static final String MOD_VERSION_STRING = "1.0.131";

	public static boolean override_config = false;
	public static double o_sv_friction = 0;
	public static double o_sv_accelerate = 0;
	public static double o_sv_airaccelerate = 0;
	public static double o_sv_maxairspeed = 0;
	public static double o_speed_mul = 0;
	public static double o_sv_gravity = 0;
	public static double o_speed_cap = 0;
	public static boolean o_hns = false;

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

		// client to server
		PayloadTypeRegistry.playC2S().register(AntiCheatPayload.ID, AntiCheatPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CSpecEfficiencyPayload.ID, CSpecEfficiencyPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(HandshakeIDPayload.ID, HandshakeIDPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MapFinishPayload.ID, MapFinishPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(OpenMapScreenPayload.ID, OpenMapScreenPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(OtherVTogglePayload.ID, OtherVTogglePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ReplayVTogglePayload.ID, ReplayVTogglePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SelfVTogglePayload.ID, SelfVTogglePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SendEfficiencyPayload.ID, SendEfficiencyPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SendMapPayload.ID, SendMapPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SendPersonalRecordPayload.ID, SendPersonalRecordPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SendRecordPayload.ID, SendRecordPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SendSpectatorsPayload.ID, SendSpectatorsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SendTimePayload.ID, SendTimePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetCheaterPayload.ID, SetCheaterPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SSpecEfficiencyPayload.ID, SSpecEfficiencyPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(UpdatePowerPayload.ID, UpdatePowerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ZoneSyncIDPayload.ID, ZoneSyncIDPayload.CODEC);
		// server to client
		PayloadTypeRegistry.playS2C().register(AntiCheatPayload.ID, AntiCheatPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CSpecEfficiencyPayload.ID, CSpecEfficiencyPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(HandshakeIDPayload.ID, HandshakeIDPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MapFinishPayload.ID, MapFinishPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenMapScreenPayload.ID, OpenMapScreenPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OtherVTogglePayload.ID, OtherVTogglePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ReplayVTogglePayload.ID, ReplayVTogglePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SelfVTogglePayload.ID, SelfVTogglePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SendEfficiencyPayload.ID, SendEfficiencyPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SendMapPayload.ID, SendMapPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SendPersonalRecordPayload.ID, SendPersonalRecordPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SendRecordPayload.ID, SendRecordPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SendSpectatorsPayload.ID, SendSpectatorsPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SendTimePayload.ID, SendTimePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SetCheaterPayload.ID, SetCheaterPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SSpecEfficiencyPayload.ID, SSpecEfficiencyPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdatePowerPayload.ID, UpdatePowerPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ZoneSyncIDPayload.ID, ZoneSyncIDPayload.CODEC);

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