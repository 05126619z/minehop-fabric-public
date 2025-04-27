package net.nerdorg.minehop.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.MinehopClient;
import net.nerdorg.minehop.entity.custom.ReplayEntity;

public class ReplayRenderer extends MobEntityRenderer<ReplayEntity, EndEntityRenderState, ReplayModel> {
    private static final Identifier TEXTURE = Identifier.of(Minehop.MOD_ID, "textures/entity/replay_texture.png");

    public ReplayRenderer(EntityRendererFactory.Context context) {
        super(context, new ReplayModel(context.getPart(ModModelLayers.REPLAY_ENTITY)), 0.001f);
    }

    @Override
    public Identifier getTexture(EndEntityRenderState entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(ReplayEntity mobEntity, Frustum frustum, double d, double e, double f) {
        return !MinehopClient.hideReplay;
    }

    @Override
    public EndEntityRenderState createRenderState() {
        return new EndEntityRenderState();
    }
}
