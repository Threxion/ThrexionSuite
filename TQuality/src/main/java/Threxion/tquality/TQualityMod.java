package Threxion.tquality;

import Threxion.tquality.definition.QualityDefinitionLoader;
import Threxion.tquality.definition.QualityAssignmentDefinitionLoader;
import Threxion.tquality.engine.QualityService;
import Threxion.tquality.scan.QualityScanProvider;
import Threxion.threxioncore.CoreRegistries;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.data.DefinitionLoaderRegistry;
import Threxion.threxioncore.api.property.PropertySchema;
import Threxion.threxioncore.api.property.PropertyType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(TQualityMod.MOD_ID)
public final class TQualityMod {
    public static final String MOD_ID = "tquality";
    public TQualityMod() {
        DefinitionLoaderRegistry.register("tquality:grade", new QualityDefinitionLoader());
        DefinitionLoaderRegistry.register("tquality:assignment", new QualityAssignmentDefinitionLoader());
        CoreRegistries.SCAN_PROVIDERS.register(PropertyId.parse("tquality:scan"), new QualityScanProvider());
        copyDefaults();
        copyDefaultGrade();
        copyDefaultWheatGrade();
        PropertyId grade = PropertyId.parse("tquality:grade");
        CoreRegistries.PROPERTIES.register(grade, PropertySchema.builder(grade)
                .type(PropertyType.STRING).defaultValue("").modifiable(false).persistent(true)
                .description("TQuality 品质等级（由 JSON 定义）").build());
        PropertyId multiplier = PropertyId.parse("tquality:quality_multiplier");
        CoreRegistries.PROPERTIES.register(multiplier, PropertySchema.builder(multiplier)
                .type(PropertyType.NUMBER).defaultValue(1.0).modifiable(false).persistent(true)
                .description("TQuality 品质倍率；未提供品质数据的材料由 TSmithing 按 1.0 处理").build());
    }

    private static void copyDefaultWheatGrade() {
        Path target = FMLPaths.CONFIGDIR.get().resolve("threxioncore").resolve(MOD_ID).resolve("grades/wheat.json");
        if (Files.exists(target)) return;
        try (InputStream in = TQualityMod.class.getResourceAsStream("/config_defaults/tquality/grades/wheat.json")) {
            if (in == null) return;
            Files.createDirectories(target.getParent());
            Files.copy(in, target);
        } catch (IOException ignored) { }
    }

    private static void copyDefaultGrade() {
        Path target = FMLPaths.CONFIGDIR.get().resolve("threxioncore").resolve(MOD_ID).resolve("grades/default.json");
        if (Files.exists(target)) return;
        try (InputStream in = TQualityMod.class.getResourceAsStream("/config_defaults/tquality/grades/default.json")) {
            if (in == null) return;
            Files.createDirectories(target.getParent());
            Files.copy(in, target);
        } catch (IOException ignored) { }
    }

    private static void copyDefaults() {
        Path target = FMLPaths.CONFIGDIR.get().resolve("threxioncore").resolve(MOD_ID).resolve("assignments/example.json");
        if (Files.exists(target)) return;
        try (InputStream in = TQualityMod.class.getResourceAsStream("/config_defaults/tquality/assignments/example.json")) {
            if (in == null) return;
            Files.createDirectories(target.getParent());
            Files.copy(in, target);
        } catch (IOException ignored) { }
    }
}
