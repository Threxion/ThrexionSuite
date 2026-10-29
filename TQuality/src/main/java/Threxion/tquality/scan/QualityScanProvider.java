package Threxion.tquality.scan;

import Threxion.tquality.engine.QualityService;
import Threxion.threxioncore.api.PropertyId;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import Threxion.threxioncore.api.property.PropertyValue;
import Threxion.threxioncore.api.scan.ScanContext;
import Threxion.threxioncore.api.scan.ScanProvider;
import Threxion.threxioncore.api.scan.ScanResult;
import net.minecraft.world.item.ItemStack;

public final class QualityScanProvider implements ScanProvider {
    private static final PropertyId ID=PropertyId.parse("tquality:scan");
    @Override public PropertyId getId(){return ID;}
    @Override public void provide(ScanContext context, ScanResult result){
        Object subject=context.getSubject().orElse(null);
        if(!(subject instanceof ItemStack stack)||stack.isEmpty())return;
        QualityService.assignIfNeeded(stack);
        ItemStackPropertyContainer props=new ItemStackPropertyContainer(stack);
        props.get(QualityService.GRADE).ifPresent(v->result.add("品质",v,"TQuality"));
        for(PropertyId id:props.getPropertyIds()){
            if(!"tquality".equals(id.getNamespace())||id.equals(QualityService.GRADE))continue;
            props.get(id).ifPresent(v->result.add("品质属性/"+id.getPath(),v,"TQuality"));
        }
    }
}
