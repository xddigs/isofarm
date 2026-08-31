package com.isofarm.graphics;

import com.isofarm.data.*;
import com.isofarm.item.*;
import com.isofarm.utils.K;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ResourceManager {
    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);
    private static SpriteSheet seedIcons;
    private static SpriteSheet cropIcons;
    private static SpriteSheet toolIcons;
    private static SpriteSheet blockIcons;
    private static SpriteSheet materialIcons;
    private static SpriteSheet usablesIcons;
    private static SpriteSheet inventoryIcons;
    private static SpriteSheet playerSpriteSheet;
    private static SpriteSheet backpackSpriteSheet;
    private static SpriteSheet bookAnimationSheet;
    private static Map<CropType, SpriteSheet> cropSpritesheets;
    private static SpriteSheet heartsSpriteSheet;
    private static SpriteSheet destroyTexture;
    private static SpriteSheet wheat;
    private static SpriteSheet carrot;
    private static SpriteSheet potato;
    private static SpriteSheet beetroot;
    private final Shader defaultShader;
    private final Shader outlineShader;
    private final Shader rainShader;
    private final Shader motionBlurShader;
    private final Shader shadowMapShader;
    private final Shader blurShader;
    private final Mesh screenQuadMesh;
    private final Mesh blockMesh;
    private final Mesh selectionMesh;
    private final Mesh spriteMesh;
    private final Mesh flowerMesh;
    private final Mesh playerMesh;
    private final Mesh destroyOverlayMesh;
    private final Texture backgroundGUI;
    private final TextureAtlas blocksAtlas;

    public ResourceManager() {
        this.defaultShader = new Shader(K.Paths.DEFAULT_VERT_SHADER, K.Paths.DEFAULT_FRAG_SHADER);
        this.outlineShader = new Shader(K.Paths.OUTLINE_VERT_SHADER, K.Paths.OUTLINE_FRAG_SHADER);
        this.rainShader = new Shader(K.Paths.RAIN_VERT_SHADER, K.Paths.RAIN_FRAG_SHADER);
        this.motionBlurShader = new Shader(K.Paths.MOTION_BLUR_VERT_SHADER, K.Paths.MOTION_BLUR_FRAG_SHADER);
        this.shadowMapShader = new Shader(K.Paths.SHADOW_VERT_SHADER, K.Paths.SHADOW_FRAG_SHADER);
        this.blurShader = new Shader(K.Paths.BLUR_VERT_SHADER, K.Paths.BLUR_FRAG_SHADER);

        this.screenQuadMesh = Mesh.screenQuad();
        this.blockMesh = Mesh.createMesh(K.World.DEFAULT_BLOCK_DEPTH);
        this.selectionMesh = Mesh.selection();
        this.spriteMesh = Mesh.createCrop();
        this.flowerMesh = Mesh.createCrossMesh();
        this.playerMesh = Mesh.quadVertical();
        this.destroyOverlayMesh = Mesh.createDestroyOverlayMesh();

        List<String> allPaths = BlockData.getAllTexturePaths();
        this.blocksAtlas = new TextureAtlas(allPaths, 16, 16);
        for (BlockData block : BlockData.values()) {
            block.initRegions(this.blocksAtlas);
        }

        this.backgroundGUI = new Texture(K.Paths.DEFAULT_BACKGROUND_GUI);

        wheat = new SpriteSheet(K.Paths.WHEAT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
        carrot = new SpriteSheet(K.Paths.CARROT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
        potato = new SpriteSheet(K.Paths.POTATO_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);
        beetroot = new SpriteSheet(K.Paths.BEETROOT_TEXTURE, K.Render.CROP_TOTAL_FRAMES, 1);

        blockIcons = new SpriteSheet(K.Paths.BLOCK_ICONS, K.UI.ICON_BLOCK_COLS, K.UI.ICON_BLOCK_ROWS);
        toolIcons = new SpriteSheet(K.Paths.TOOL_ICONS, K.UI.ICON_TOOL_COLS, K.UI.ICON_TOOL_ROWS);
        cropIcons = new SpriteSheet(K.Paths.CROP_ICONS, K.UI.ICON_SEED_CROPS_COLS, 1);
        seedIcons = new SpriteSheet(K.Paths.SEED_ICONS, K.UI.ICON_SEED_CROPS_COLS, 1);
        materialIcons = new SpriteSheet(K.Paths.MATERIAL_ICONS, K.UI.ICON_MATERIAL_COLS, K.UI.ICON_MATERIAL_ROWS);
        usablesIcons = new SpriteSheet(K.Paths.USABLES_ICONS, K.UI.ICON_USABLES_COLS, 1);
        inventoryIcons = new SpriteSheet(K.Paths.INVENTORY_ICONS, K.UI.ICON_INV_COLS, 1);

        playerSpriteSheet = new SpriteSheet(K.Paths.PLAYER_SPRITESHEET, K.UI.PLAYER_SPRITE_COLS, K.UI.PLAYER_SPRITE_ROWS);
        backpackSpriteSheet = new SpriteSheet(K.Paths.BACKPACK_SPRITESHEET, K.UI.PLAYER_SPRITE_COLS / 3, 2);
        bookAnimationSheet = new SpriteSheet(K.Paths.BOOK_ANIMATION, 16, 1);
        heartsSpriteSheet = new SpriteSheet(K.Paths.HEARTS_SPRITESHEET, 1, K.UI.ICON_HEARTS_ROWS);

        destroyTexture = new SpriteSheet(K.Paths.DESTROY_STAGES, K.UI.DESTROY_FRAMES, 1);

        cropSpritesheets = new EnumMap<>(CropType.class);
        cropSpritesheets.put(CropType.WHEAT, wheat);
        cropSpritesheets.put(CropType.CARROT, carrot);
        cropSpritesheets.put(CropType.POTATO, potato);
        cropSpritesheets.put(CropType.BEETROOT, beetroot);
    }

    public static SpriteSheet getItemSpriteSheet(Item item) {
        return switch (item) {
            case Crop crop -> cropSpritesheets.get(crop.getCropType());
            case Produce ignored -> cropIcons;
            case Seed ignored -> seedIcons;
            case Tool ignored -> toolIcons;
            case Material ignored -> materialIcons;
            case Usable ignored -> usablesIcons;
            case Block ignored -> blockIcons;
            case null, default -> null;
        };
    }

    private static int getMaterialFrame(Material material) {
        MaterialID materialID = material.getMaterialID();
        int row = materialID.getRow();
        if (row == 1) {
            int col = switch (materialID) {
                case PAPER -> 1;
                case LEATHER -> 2;
                default -> 0;
            };
            return (row * K.UI.ICON_MATERIAL_COLS) + col;
        }

        if (material.getTier() != null) {
            int baseCol = switch (material.getTier()) {
                case IRON -> 2;
                case PLATINUM -> 4;
                case GOLD -> 6;
                case STEEL -> 8;
                case DIAMOND -> 10;
                default -> 0;
            };

            int column = (materialID == MaterialID.INGOT) ? baseCol + 1 : baseCol;
            return (row * K.UI.ICON_MATERIAL_COLS) + column;
        }
        return 0;
    }

    public static int getItemFrame(Item item) {
        if (item instanceof Block block && block.getType() != null) {
            int col = block.getType().getCol() - 1;
            int row = block.getType().getRow();
            return (row * K.UI.ICON_BLOCK_COLS) + col;
        }

        if (item instanceof Produce produce && produce.getType() != null) {
            return produce.getType().getId();
        }

        if (item instanceof Seed seed && seed.getType() != null) {
            return seed.getType().getId();
        }

        if (item instanceof Crop crop && crop.getCropType() != null) {
            return crop.getCropType().getId();
        }

        if (item instanceof Tool tool) {
            int row = tool.getRow();
            int col = tool.getCol();
            return (row * K.UI.ICON_TOOL_COLS) + col;
        }

        if (item instanceof Usable usable) {
            int col = usable.getUsablesID().getCol();
            int row = usable.getUsablesID().getRow();
            int bucketOffset = (usable instanceof Bucket bucket && bucket.isFull()) ? 1 : 0;
            return (row * K.UI.ICON_USABLES_COLS) + col + bucketOffset;
        }

        if (item instanceof Material material) {
            return getMaterialFrame(material);
        }

        return 0;
    }

    public static SpriteSheet getPlayerSpriteSheet() {
        return playerSpriteSheet;
    }

    public static SpriteSheet getBackpackSpriteSheet() {
        return backpackSpriteSheet;
    }

    public void dispose() {
        blockMesh.dispose();
        flowerMesh.dispose();
        selectionMesh.dispose();
        spriteMesh.dispose();
        screenQuadMesh.dispose();
        playerMesh.dispose();
        destroyOverlayMesh.dispose();

        backgroundGUI.dispose();
        blocksAtlas.dispose();

        wheat.dispose();
        carrot.dispose();
        potato.dispose();
        beetroot.dispose();

        cropIcons.dispose();
        seedIcons.dispose();
        toolIcons.dispose();
        blockIcons.dispose();
        materialIcons.dispose();
        usablesIcons.dispose();
        inventoryIcons.dispose();

        playerSpriteSheet.dispose();
        backpackSpriteSheet.dispose();
        bookAnimationSheet.dispose();
        heartsSpriteSheet.dispose();

        defaultShader.dispose();
        outlineShader.dispose();
        motionBlurShader.dispose();
        rainShader.dispose();
        shadowMapShader.dispose();
        blurShader.dispose();
    }

    public Shader getDefaultShader() {
        return defaultShader;
    }

    public Shader getOutlineShader() {
        return outlineShader;
    }

    public Shader getRainShader() {
        return rainShader;
    }

    public Shader getMotionBlurShader() {
        return motionBlurShader;
    }

    public Shader getShadowMapShader() {
        return shadowMapShader;
    }

    public Shader getBlurShader() {
        return blurShader;
    }

    public Mesh getScreenQuadMesh() {
        return screenQuadMesh;
    }

    public Mesh getBlockMesh() {
        return blockMesh;
    }

    public Mesh getFlowerMesh() {
        return flowerMesh;
    }

    public Mesh getDestroyOverlayMesh() {
        return destroyOverlayMesh;
    }

    public Mesh getSelectionMesh() {
        return selectionMesh;
    }

    public Mesh getSpriteMesh() {
        return spriteMesh;
    }

    public Mesh getPlayerMesh() {
        return playerMesh;
    }

    public Texture getBackgroundGUI() {
        return backgroundGUI;
    }

    public TextureAtlas getBlocksAtlas() {
        return blocksAtlas;
    }

    public static SpriteSheet getDestroyTexture() {
        return destroyTexture;
    }

    public static SpriteSheet getSeedIcons() {
        return seedIcons;
    }

    public static SpriteSheet getCropIcons() {
        return cropIcons;
    }

    public static SpriteSheet getToolIcons() {
        return toolIcons;
    }

    public static SpriteSheet getBlockIcons() {
        return blockIcons;
    }

    public static SpriteSheet getMaterialIcons() {
        return materialIcons;
    }

    public static SpriteSheet getUsablesIcons() {
        return usablesIcons;
    }

    public static SpriteSheet getInventoryIcons() {
        return inventoryIcons;
    }

    public static SpriteSheet getBookAnimationSheet() {
        return bookAnimationSheet;
    }

    public SpriteSheet getHeartsSpriteSheet() {
        return heartsSpriteSheet;
    }

    public Map<CropType, SpriteSheet> getCropSpritesheets() {
        return cropSpritesheets;
    }

    public Shader getShader(String name) {
        if (name == null) return defaultShader;
        return switch (name.toLowerCase()) {
            case "outline" -> outlineShader;
            case "rain" -> rainShader;
            case "motion_blur" -> motionBlurShader;
            case "blur" -> blurShader;
            case "shadow" -> shadowMapShader;
            case "default", "item" -> defaultShader;
            default -> {
                log.warn("Shader '{}' not found, using defaultShader", name);
                yield defaultShader;
            }
        };
    }
}
