package dev.aptitudes.client.screen;

import dev.aptitudes.client.contract.ContractClientState;
import dev.aptitudes.network.ContractActionPayload;
import dev.aptitudes.network.ContractViewPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ContractBoardScreen extends HandledScreen<dev.aptitudes.contract.ContractBoardScreenHandler> {
	public ContractBoardScreen(dev.aptitudes.contract.ContractBoardScreenHandler handler, PlayerInventory inventory, Text title){super(handler,inventory,title);}
	@Override protected void init(){this.backgroundWidth=420;this.backgroundHeight=230;super.init();this.titleX=8;this.playerInventoryTitleY=1000;
		for(int i=0;i<3;i++){final int index=i;addDrawableChild(ButtonWidget.builder(Text.translatable("contract.action.accept"),b->action(ContractActionPayload.ACCEPT,index)).dimensions(x+329,y+54+i*24,80,18).build());
			addDrawableChild(ButtonWidget.builder(Text.translatable("contract.action.abandon"),b->action(ContractActionPayload.ABANDON,index)).dimensions(x+235,y+139+i*24,55,18).build());
			addDrawableChild(ButtonWidget.builder(Text.translatable("contract.action.deliver"),b->action(ContractActionPayload.DELIVER,index)).dimensions(x+292,y+139+i*24,55,18).build());
			addDrawableChild(ButtonWidget.builder(Text.translatable("contract.action.claim"),b->action(ContractActionPayload.CLAIM,index)).dimensions(x+349,y+139+i*24,55,18).build());}
		ClientPlayNetworking.send(new ContractActionPayload(ContractActionPayload.REQUEST,0));}
	private void action(int type,int index){ClientPlayNetworking.send(new ContractActionPayload(type,index));ClientPlayNetworking.send(new ContractActionPayload(ContractActionPayload.REQUEST,0));}
	@Override protected void drawBackground(DrawContext c,float delta,int mouseX,int mouseY){c.fill(x,y,x+backgroundWidth,y+backgroundHeight,0xE5221710);c.fill(x+3,y+3,x+backgroundWidth-3,y+backgroundHeight-3,0xEE33251B);}
	@Override public void render(DrawContext c,int mouseX,int mouseY,float delta){renderBackground(c,mouseX,mouseY,delta);super.render(c,mouseX,mouseY,delta);ContractViewPayload view=ContractClientState.get();
		if(view==null){c.drawCenteredTextWithShadow(textRenderer,Text.translatable("contract.loading"),x+backgroundWidth/2,y+45,0xFFFFFF);return;}
		c.drawTextWithShadow(textRenderer,Text.translatable("contract.rank",view.rank(),view.reputation()),x+10,y+20,0xF2D28A);
		int yy=y+43;int row=0;
		for(ContractViewPayload.Row r:view.offers()) {int cy=yy+row*26;c.drawTextWithShadow(textRenderer,label(r,false),x+10,cy,0xFFFFFF);row++;}
		int base=y+126;row=0;
		for(ContractViewPayload.Row r:view.active()){int cy=base+row*27;c.drawTextWithShadow(textRenderer,label(r,true),x+10,cy,0xBFE9C7);row++;}
	}
	private Text label(ContractViewPayload.Row r,boolean active){String[] kinds={"Caza","Entrega","Pesca","Encantamiento","Oficio","Forja","Evento","Jefe"};String[] diffs={"Fácil","Normal","Difícil","Élite","Legendario"};String k=kinds[Math.max(0,Math.min(kinds.length-1,r.kind()))];String d=diffs[Math.max(0,Math.min(diffs.length-1,r.difficulty()))];return Text.literal((active?"● ":"○ ")+d+" · "+k+" · "+r.progress()+"/"+r.goal()+" · ♦"+r.emeralds());}
	@Override public void close(){ContractClientState.clear();super.close();}
}
