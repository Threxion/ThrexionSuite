package Threxion.talchemy.definition;
import Threxion.threxioncore.api.PropertyId; import Threxion.threxioncore.api.data.Definition; import java.util.Objects;
/** 工作方块提供 Process；Tier 由 Station 提供，并且只在 Recipe 标记为成药环节时参与。 */
public record StationDefinition(PropertyId id,String block,String displayName,String process,boolean tierEnabled,String tier,double timeMultiplier) implements Definition {
 public StationDefinition { Objects.requireNonNull(id); block=block==null?"":block; displayName=displayName==null?"":displayName; process=process==null?"":process; tier=(tier==null||tier.isBlank())?"talchemy:wilderness":tier; timeMultiplier=timeMultiplier<=0?1.0:timeMultiplier; }
 @Override public PropertyId getId(){return id;}
}
