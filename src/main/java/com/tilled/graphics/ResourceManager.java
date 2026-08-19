package com.tilled.graphics;

import com.tilled.data.*;
import com.tilled.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public class ResourceManager {
    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);

    private final Shader defaultShader;
    private final Shader outlineShader;
    private final Shader rainShader;
    private final Shader motionBlurShader;
    private final Shader shadowMapShader;

    private final Mesh screenQuadMesh;
    private final Mesh blockMesh;
    private final Mesh selectionMesh;
    private final Mesh spriteMesh;

    private final SpriteSheet blocksTexture;
    private final SpriteSheet waterTexture;
    private final SpriteSheet wheat;
    private final SpriteSheet carrot;
    private final SpriteSheet potato;
    private final SpriteSheet beetroot;
    private final SpriteSheet seedIcons;
    private final SpriteSheet cropIcons;
    private final SpriteSheet toolIcons;
    private final SpriteSheet blockIcons;
    private final SpriteSheet inventoryIcons;

    private final Map<CropType, SpriteSheet> cropSpritesheets;

    public ResourceManager() {
        this.defaultShader = new Shader(K.Paths.DEFAULT_VERT_SHADER, K.Paths.DEFAULT_FRAG_SHADER);
        this.outlineShader = new Shader(K.Paths.OUTLINE_VERT_SHADER, K.Paths.OUTLINE_FRAG_SHADER);
        this.rainShader = new Shader(K.Paths.RAIN_VERT_SHADER, K.Paths.RAIN_FRAG_SHADER);
        this.motionBlurShader = new Shader(K.Paths.MOTION_BLUR_VERT_SHADER, K.Paths.MOTION_BLUR_FRAG_SHADER);
        this.shadowMapShader = new Shader(K.Paths.SHADOW_VERT_SHADER, K.Paths.SHADOW_FRAG_SHADER);

        this.screenQuadMesh = Mesh.screenQuad();
        this.blockMesh = Mesh.createMesh(K.World.DEFAULT_BLOCK_DEPTH);
        this.selectionMesh = Mesh.selection();
        this.spriteMesh = Mesh.createCrop();

        SpriteSheet blocks = null;
        try {
            blocks = new SpriteSheet(K.Paths.BLOCKS, K.UI.BLOCK_ATLAS_FRAMES);
        } catch (Exception e) {
            log.warn("Could not load blocks.png atlas, falling back to base colors: {}", e.getMessage());
        }
        this.blocksTexture = blocks;

        this.waterTexture = new SpriteSheet(K.Paths.WATER, K.UI.WATER_FRAMES);
        this.wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES);
        this.beetroot = new SpriteSheet(K.Paths.BEETROOT_TEXTURE, K.Render.CROP_TOTAL_FRAMES);

        this.seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_SEED_CROPS_FRAMES);
        this.cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_SEED_CROPS_FRAMES);
        this.toolIcons = new SpriteSheet(K.Paths.TOOL_ICONS, K.UI.ICON_TOOL_FRAMES);
        this.blockIcons = new SpriteSheet(K.Paths.BLOCK_ICONS, K.UI.ICON_BLOCK_FRAMES);
        this.inventoryIcons = new SpriteSheet(K.Paths.INVENTORY_ICONS, 2);

        this.cropSpritesheets = new EnumMap<>(CropType.class);
        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);
        cropSpritesheets.put(CropType.BEETROOT, beetroot);
    }

    public void dispose() {
        blockMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();

        if (blocksTexture != null) blocksTexture.dispose();
        if (waterTexture != null) waterTexture.dispose();

        wheat.dispose();
        carrot.dispose();
        potato.dispose();
        beetroot.dispose();

        cropIcons.dispose();
        seedIcons.dispose();
        toolIcons.dispose();
        blockIcons.dispose();
        inventoryIcons.dispose();

        defaultShader.dispose();
        outlineShader.dispose();
        motionBlurShader.dispose();
        rainShader.dispose();
        shadowMapShader.dispose();
    }

    public Shader getDefaultShader() { return defaultShader; }
    public Shader getOutlineShader() { return outlineShader; }
    public Shader getRainShader() { return rainShader; }
    public Shader getMotionBlurShader() { return motionBlurShader; }
    public Shader getShadowMapShader() { return shadowMapShader; }
    public Mesh getScreenQuadMesh() { return screenQuadMesh; }
    public Mesh getBlockMesh() { return blockMesh; }
    public Mesh getSelectionMesh() { return selectionMesh; }
    public Mesh getSpriteMesh() { return spriteMesh; }
    public SpriteSheet getBlocksTexture() { return blocksTexture; }
    public SpriteSheet getWaterTexture() { return waterTexture; }
    public SpriteSheet getSeedIcons() { return seedIcons; }
    public SpriteSheet getCropIcons() { return cropIcons; }
    public SpriteSheet getToolIcons() { return toolIcons; }
    public SpriteSheet getBlockIcons() { return blockIcons; }
    public SpriteSheet getInventoryIcons() { return inventoryIcons; }
    public Map<CropType, SpriteSheet> getCropSpritesheets() { return cropSpritesheets; }

    public SpriteSheet getItemSpriteSheet(Item item) {
        return switch (item) {
            case null -> null;
            case Crop crop -> cropSpritesheets.get(crop.getCropType());
            case Seed ignored -> seedIcons;
            case Tool ignored -> toolIcons;
            default -> blockIcons;
        };
    }

    public Shader getShader(String name) {
        if (name == null)  return defaultShader;
        return switch (name.toLowerCase()) {
            case "outline" -> outlineShader;
            case "rain" -> rainShader;
            case "motion_blur" -> motionBlurShader;
            case "shadow" -> shadowMapShader;
            case "default", "item" -> defaultShader;
            default -> {
                log.warn("Shader '{}' not found, using defaultShader", name);
                yield defaultShader;
            }
        };
    }
}
