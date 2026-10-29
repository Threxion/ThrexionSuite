package Threxion.talchemy.definition;
import Threxion.threxioncore.api.PropertyId; import Threxion.threxioncore.api.data.DefinitionLoader; import com.google.gson.JsonObject; import net.minecraft.util.GsonHelper; import java.nio.file.Path; import java.util.Set;
public final class StationDefinitionLoader implements DefinitionLoader<StationDefinition>{
 public PropertyId getType(){return PropertyId.parse("talchemy:station");}
 public Set<String> getDirectories(){return Set.of("talchemy/stations");}
 public StationDefinition load(Path file,JsonObject obj){return new StationDefinition(PropertyId.parse(GsonHelper.getAsString(obj,"id")),GsonHelper.getAsString(obj,"block",""),GsonHelper.getAsString(obj,"display_name",""),GsonHelper.getAsString(obj,"process",""),GsonHelper.getAsBoolean(obj,"tier_enabled",true),GsonHelper.getAsString(obj,"tier","talchemy:wilderness"), GsonHelper.getAsDouble(obj,"time_multiplier",1.0));}
 public void validate(StationDefinition d){if(d.block().isBlank())throw new IllegalArgumentException("Station block is required: "+d.getId());if(d.process().isBlank())throw new IllegalArgumentException("Station process is required: "+d.getId());}
}
