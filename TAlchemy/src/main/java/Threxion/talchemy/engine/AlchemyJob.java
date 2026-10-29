package Threxion.talchemy.engine;

import Threxion.talchemy.api.IAlchemyStation;
import Threxion.talchemy.definition.AlchemyDefinition;
import Threxion.talchemy.definition.ProcessDefinition;
import Threxion.threxioncore.api.property.ItemStackPropertyContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** 一个由工作方块执行的炼金流程；输入、催化剂和输出均属于 Station。 */
public final class AlchemyJob {
    private final ServerPlayer player;
    private final IAlchemyStation station;
    private final AlchemyDefinition recipe;
    private final ProcessDefinition process;
    private final ItemStack catalyst;
    private final ItemStack workingOutput;
    private int elapsedTicks;
    private int stageIndex;
    private boolean failed;
    private final boolean inputWasExpired;
    public AlchemyJob(ServerPlayer player, IAlchemyStation station, AlchemyDefinition recipe, ProcessDefinition process, ItemStack catalyst, ItemStack workingOutput, boolean inputWasExpired) {
        this.player=player; this.station=station; this.recipe=recipe; this.process=process;
        this.catalyst=catalyst==null?ItemStack.EMPTY:catalyst.copy(); this.workingOutput=workingOutput;
        this.inputWasExpired=inputWasExpired;
    }
    public ServerPlayer player(){return player;} public IAlchemyStation station(){return station;}
    public String stationId(){return station.stationId();} public AlchemyDefinition recipe(){return recipe;}
    public ProcessDefinition process(){return process;} public ItemStack catalyst(){return catalyst;}
    public ItemStack workingOutput(){return workingOutput;} public int elapsedTicks(){return elapsedTicks;}
    public void tick(){elapsedTicks++;} public int stageIndex(){return stageIndex;} public void stageIndex(int v){stageIndex=v;}
    public boolean failed(){return failed;} public void markFailed(){failed=true;}
    public boolean inputWasExpired(){return inputWasExpired;}
    public ItemStackPropertyContainer outputProperties(){return new ItemStackPropertyContainer(workingOutput);}
}
