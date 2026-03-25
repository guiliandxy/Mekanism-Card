package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MekanismCard.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private static final int MAX_RENDER = 500;

    private ClientEvents() {}

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack mainHand = mc.player.getMainHandItem();
        if (mainHand.getItem() instanceof MassUpgradeConfigurator configurator) {
            if (configurator.isSelectionModeActive(mainHand)) {
                float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.3f + 0.7f);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.depthMask(false);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                float r = 0.0f, g = 1.0f, b = 1.0f;
                Matrix4f matrix = event.getPoseStack().last().pose();
                Tesselator t = Tesselator.getInstance();
                BufferBuilder buf = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                float size = 0.5f;
                float thickness = 0.02f;
                float z = 0.01f;

                buf.addVertex(matrix, -thickness, -thickness, z).setColor(r, g, b, 0.6f * pulse);
                buf.addVertex(matrix, size + thickness, -thickness, z).setColor(r, g, b, 0.6f * pulse);
                buf.addVertex(matrix, size + thickness, size + thickness, z).setColor(r, g, b, 0.6f * pulse);
                buf.addVertex(matrix, -thickness, size + thickness, z).setColor(r, g, b, 0.6f * pulse);

                buf.addVertex(matrix, size, 0, z).setColor(r, g, b, 0.6f * pulse);
                buf.addVertex(matrix, size, size, z).setColor(r, g, b, 0.6f * pulse);
                buf.addVertex(matrix, 0, size, z).setColor(r, g, b, 0.6f * pulse);
                buf.addVertex(matrix, 0, 0, z).setColor(r, g, b, 0.6f * pulse);

                BufferUploader.drawWithShader(buf.build());
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof MassUpgradeConfigurator configurator)) return;

        if (!configurator.isSelectionModeActive(mainHand)) return;
        BlockPos[] points = configurator.getSelectionPoints(mainHand);
        if (points[0] == null || points[1] == null) return;

        // 获取当前模式和背包升级类型
        MassUpgradeConfigurator.Mode currentMode = configurator.getCurrentMode();
        Upgrade upgradeType = configurator.getSelectedUpgradeFromInventory(player);
        boolean hasUpgrade = upgradeType != null;

        // 基础颜色：无升级模块时灰色；有模块时根据模式决定
        float r, g, b;
        if (!hasUpgrade) {
            r = g = b = 0.5f; // 灰色
        } else {
            if (currentMode == MassUpgradeConfigurator.Mode.INSTALL) {
                r = 0.0f; g = 1.0f; b = 0.0f; // 绿色
            } else {
                r = 1.0f; g = 0.0f; b = 0.0f; // 红色
            }
        }

        // 计算选区范围
        int minX = Math.min(points[0].getX(), points[1].getX());
        int maxX = Math.max(points[0].getX(), points[1].getX());
        int minY = Math.min(points[0].getY(), points[1].getY());
        int maxY = Math.max(points[0].getY(), points[1].getY());
        int minZ = Math.min(points[0].getZ(), points[1].getZ());
        int maxZ = Math.max(points[0].getZ(), points[1].getZ());

        Camera camera = event.getCamera();
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        int count = 0;

        // 遍历选区内的所有方块
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (count++ >= MAX_RENDER) {
                        // 超过最大渲染数量，提前结束（但保持代码简洁，只跳出循环）
                        // 这里简单处理，直接返回（避免复杂跳出）
                        // 实际更好的做法是使用标记跳出多层循环，但为了简洁，我们限制数量后直接返回。
                        // 注意：直接返回可能导致部分机器未渲染，但这是性能保护，可以接受。
                        return;
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    // 只渲染 Mekanism 机器
                    if (be instanceof TileEntityMekanism) {
                        AABB worldBox = new AABB(x, y, z, x + 1, y + 1, z + 1);
                        AABB relativeBox = worldBox.move(-camX, -camY, -camZ);
                        renderSingleBox(event.getPoseStack(), relativeBox, r, g, b);
                    }
                }
            }
        }
    }

    // ======================================================
    // 渲染单个盒子的边框 + 半透明填充（无呼吸动画，简洁）
    // ======================================================
    private static void renderSingleBox(PoseStack poseStack, AABB box, float r, float g, float b) {
        // 轻微膨胀，防 Z-fighting
        AABB inflated = box.inflate(0.002);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();

        // 边框 (LINES)
        BufferBuilder lineBuf = t.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        LevelRenderer.renderLineBox(poseStack, lineBuf, inflated, r, g, b, 1.0F);
        BufferUploader.drawWithShader(lineBuf.build());

        // 半透明填充 (QUADS)
        BufferBuilder fillBuf = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillBox(fillBuf, matrix, inflated, r, g, b, 0.10F);
        BufferUploader.drawWithShader(fillBuf.build());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // 填充立方体的六个面
    private static void fillBox(BufferBuilder buf, Matrix4f m, AABB b,
                                float r, float g, float bl, float a) {
        double x1 = b.minX, y1 = b.minY, z1 = b.minZ;
        double x2 = b.maxX, y2 = b.maxY, z2 = b.maxZ;

        // 底面
        quad(buf, m, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, bl, a);
        // 顶面
        quad(buf, m, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, r, g, bl, a);
        // 四个侧面
        quad(buf, m, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, r, g, bl, a);
        quad(buf, m, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, bl, a);
        quad(buf, m, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, bl, a);
        quad(buf, m, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, r, g, bl, a);
    }

    private static void quad(BufferBuilder b, Matrix4f m,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             double x3, double y3, double z3,
                             double x4, double y4, double z4,
                             float r, float g, float bl, float a) {
        v(b, m, x1, y1, z1, r, g, bl, a);
        v(b, m, x2, y2, z2, r, g, bl, a);
        v(b, m, x3, y3, z3, r, g, bl, a);
        v(b, m, x4, y4, z4, r, g, bl, a);
    }

    private static void v(BufferBuilder b, Matrix4f m,
                          double x, double y, double z,
                          float r, float g, float bl, float a) {
        b.addVertex(m, (float) x, (float) y, (float) z)
                .setColor(r, g, bl, a);
    }
}