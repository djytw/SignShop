
package org.wargamer2010.signshop.blocks;

import com.google.common.collect.ImmutableList;
import com.meowj.langutils.lang.LanguageHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.wargamer2010.signshop.configuration.ColorUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.itemUtil;
import static org.wargamer2010.signshop.util.itemUtil.enchantmentsToMessageFormat;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopItemMeta {
    private static final String listSeperator = "~";
    private static final String valueSeperator = "-";
    private static final String innerListSeperator = "^";
    private static final ChatColor txtColor = ChatColor.YELLOW;
    private static final String filename = "books.db";
    private static final String[] potion_effects = {
        "",".speed", ".slowness", ".haste", ".mining_fatigue", ".strength", ".instant_health", ".instant_damage", ".jump_boost", 
        ".nausea", ".regeneration", ".resistance", ".fire_resistance", ".water_breathing", ".invisibility", ".blindness", 
        ".night_vision", ".hunger", ".weakness", ".poison", ".wither", ".health_boost", ".absorption", ".saturation", 
        ".glowing", ".levitation", ".luck", ".unluck", ".slow_falling", ".conduit_power", ".dolphins_grace", ".bad_omen"
    };
    private static final int[][] potion_time = {
        {0,0,0},//UNCR
        {0,0,0},//WATE
        {0,0,0},//MUND
        {0,0,0},//THIC
        {0,0,0},//AWKW
        {3600,9600,0},//NIGH
        {3600,9600,0},//INVI
        {3600,9600,1800},//JUMP
        {3600,9600,0},//FIRE
        {3600,9600,1800},//SPEE
        {1800,4800,400},//SLOW
        {3600,9600,0},//WATE
        {0,0,0},//INST
        {0,0,0},//INST
        {900,1800,432},//POIS
        {900,1800,432},//REGE
        {3600,9600,1800},//STRE
        {1800,4800,0},//WEAK
        {6000,0,0},//LUCK
        {400,800,400},//TURT
        {1800,4800,0}//SLOW
    };
    private SignShopItemMeta() {

    }

    public static void init() {
        SSDatabase db = new SSDatabase(filename);

        try {
            if(!db.tableExists("ItemMeta"))
                db.runStatement("CREATE TABLE ItemMeta ( ItemMetaID INTEGER, ItemMetaHash INT, PRIMARY KEY(ItemMetaID) )", null, false);
            if(!db.tableExists("MetaProperty"))
                db.runStatement("CREATE TABLE MetaProperty ( PropertyID INTEGER, ItemMetaID INTEGER, PropertyName TEXT NOT NULL, ProperyValue TEXT NOT NULL, PRIMARY KEY(PropertyID) )", null, false);
        } finally {
            db.close();
        }
    }

    public static String convertColorsToDisplay(List<Color> colors) {
        if(colors == null || colors.isEmpty())
            return "";
        List<String> temp = new LinkedList<String>();

        for(Color color : colors) {
            temp.add(ColorUtil.getColorAsString(color));
        }

        String[] arr = new String[temp.size()];
        return signshopUtil.implode(temp.toArray(arr), ", ");
    }

    public static ChatColor getTextColor() {
        return txtColor;
    }

    private static String convertFireworkTypeToDisplay(FireworkEffect.Type type) {
        String t = "item.minecraft.firework_star.shape";
        switch(type){
            case STAR: t+=".star";break;
            case BALL: t+=".small_ball";break;
            case BALL_LARGE: t+=".large_ball";break;
            case BURST: t+=".burst";break;
            case CREEPER: t+=".creeper";break;
        }
        return LanguageHelper.translateToLocal(t, SignShopConfig.getPreferredLanguage());
    }

    private static boolean hasMeta(ItemStack stack) {
        // This seems silly but some parts of the code below function when an item has no meta data but itemmeta is not null
        return (stack.getItemMeta() != null);
    }

    private static String getDisplayName(ItemStack stack) {
        return getDisplayName(stack, txtColor);
    }

    private static String getDisplayName(ItemStack stack, ChatColor color) {
        String txtcolor = txtColor.toString();
        String enccolor = ChatColor.LIGHT_PURPLE.toString();
        String durcolor = ChatColor.GREEN.toString();
        String customcolor = (stack.getEnchantments().isEmpty() ? color.toString() : (ChatColor.AQUA.toString()+ChatColor.ITALIC.toString()));
        String normal = LanguageHelper.getItemName(stack,SignShopConfig.getPreferredLanguage());
        String displayname = "";

        if(stack.getItemMeta() != null) {
            String custom = (stack.getItemMeta().hasDisplayName()
                        ? (customcolor + "\""+stack.getItemMeta().getDisplayName() + "\"" + txtcolor) : "");
            if(custom.length() > 0)
                displayname = (normal + custom + txtcolor);
        }
        
        if(displayname.isEmpty())
            displayname = (txtcolor + customcolor + normal + txtcolor);

        if(stack.hasItemMeta() && stack.getItemMeta().isUnbreakable()){
            displayname = displayname + durcolor + "[" +LanguageHelper.translateToLocal("item.unbreakable",SignShopConfig.getPreferredLanguage()) + "]" + txtcolor;
        }
        else if(((Damageable)stack.getItemMeta()).hasDamage())
            displayname = (displayname + durcolor + "[" +
            String.format(LanguageHelper.translateToLocal("item.durability", SignShopConfig.getPreferredLanguage()),
                (stack.getType().getMaxDurability()-((Damageable)stack.getItemMeta()).getDamage()), stack.getType().getMaxDurability())
                 + "]" + txtcolor);
        if(stack.getEnchantments().size() > 0)
            displayname += (enccolor + " " + enchantmentsToMessageFormat(stack.getEnchantments()));

        return displayname;
    }

    public static String getName(ItemStack stack) {
        if(!hasMeta(stack))
            return getDisplayName(stack);

        ItemMeta meta = stack.getItemMeta();

        List<MetaType> metatypes = getTypesOfMeta(meta);
        for(MetaType type : metatypes) {
            if(type == MetaType.EnchantmentStorage) {
                EnchantmentStorageMeta enchantmeta = (EnchantmentStorageMeta) meta;
                if(enchantmeta.hasStoredEnchants())
                    return (getDisplayName(stack) + " " + itemUtil.enchantmentsToMessageFormat(enchantmeta.getStoredEnchants()));
            } else if(type == MetaType.LeatherArmor) {
                LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                return (LanguageHelper.translateToLocal("color.minecraft."+ColorUtil.getColorAsString(leathermeta.getColor()).toLowerCase(), SignShopConfig.getPreferredLanguage()) + getDisplayName(stack));
            } else if(type == MetaType.Skull) {
                SkullMeta skullmeta = (SkullMeta) meta;
                if(skullmeta.getOwningPlayer() != null) {
                    // Name coloring support had to be dropped since there is no more link between
                    // the skull owner and the actual player
                    return String.format(
                        LanguageHelper.translateToLocal("block.minecraft.player_head.named", SignShopConfig.getPreferredLanguage()),
                        skullmeta.getOwningPlayer().getName());
                } else {
                    // We can no longer get a pretty name by ID (SKULL_ITEM isn't pretty, is it?)
                    // So we'll have to rely on the web lookup, if the server owner has it enabled
                    return getDisplayName(stack);
                }
            } else if(type == MetaType.Potion) {
                PotionMeta potionmeta = (PotionMeta) meta;
                StringBuilder namebuilder = new StringBuilder(512);
                namebuilder.append(getDisplayName(stack, ChatColor.AQUA));

                PotionData pd = potionmeta.getBasePotionData();
                if(pd.isUpgraded()){
                    int ptime = potion_time[pd.getType().ordinal()][2];
                    if(ptime == 0)
                        namebuilder.append(" II ");
                    else
                        namebuilder.append(String.format(" II (%02d:%02d)", ptime/1200, ptime/20%60));
                }
                else if(pd.isExtended()){
                    int ptime = potion_time[pd.getType().ordinal()][1];
                    if(ptime == 0)
                        namebuilder.append(" ");
                    else
                        namebuilder.append(String.format(" (%02d:%02d)", ptime/1200, ptime/20%60));
                }else{
                    int ptime = potion_time[pd.getType().ordinal()][0];
                    if(ptime == 0)
                        namebuilder.append(" ");
                    else
                        namebuilder.append(String.format(" (%02d:%02d)", ptime/1200, ptime/20%60));
                }
                if(potionmeta.hasCustomEffects()){
                    for (PotionEffect pe : potionmeta.getCustomEffects()){
                        namebuilder.append(ChatColor.LIGHT_PURPLE.toString()+" (");
                        namebuilder.append(LanguageHelper.translateToLocal("effect.minecraft"+potion_effects[pe.getType().hashCode()], SignShopConfig.getPreferredLanguage()));
                        namebuilder.append(" "+itemUtil.binaryToRoman(pe.getAmplifier()+1));
                        namebuilder.append(String.format(" %02d:%02d", pe.getDuration()/1200, pe.getDuration()/20%60));
                        /*
                        //do not display about particles
                        namebuilder.append("["+LanguageHelper.translateToLocal("options.particles", SignShopConfig.getPreferredLanguage())+":");
                        if(!pe.hasParticles()) namebuilder.append(LanguageHelper.translateToLocal("options.particles.minimal", SignShopConfig.getPreferredLanguage()));
                        else if(pe.isAmbient()) namebuilder.append(LanguageHelper.translateToLocal("options.particles.decreased", SignShopConfig.getPreferredLanguage()));
                        else namebuilder.append(LanguageHelper.translateToLocal("options.particles.all", SignShopConfig.getPreferredLanguage()));
                        namebuilder.append("]");
                        */
                        namebuilder.append(")");
                    }
                }
                return namebuilder.toString();
            } else if(type == MetaType.Fireworks) {
                FireworkMeta fireworkmeta = (FireworkMeta) meta;

                StringBuilder namebuilder = new StringBuilder(256);
                namebuilder.append(getDisplayName(stack));
                namebuilder.append(ChatColor.AQUA.toString());
                namebuilder.append(" (");
                namebuilder.append(LanguageHelper.translateToLocal("item.minecraft.firework_rocket.flight", SignShopConfig.getPreferredLanguage()));
                namebuilder.append(fireworkmeta.getPower());

                if(fireworkmeta.hasEffects()) {
                    for(FireworkEffect effect : fireworkmeta.getEffects()) {
                        namebuilder.append(", ");

                        namebuilder.append(convertFireworkTypeToDisplay(effect.getType()));
                        namebuilder.append((effect.getColors().size() > 0 ? ", " : ""));
                        namebuilder.append(convertColorsToDisplay(effect.getColors()));
                        namebuilder.append((effect.getFadeColors().size() > 0 ?
                            ", " + LanguageHelper.translateToLocal("item.minecraft.firework_star.fade_to", SignShopConfig.getPreferredLanguage()) : ""));
                        namebuilder.append(convertColorsToDisplay(effect.getFadeColors()));
                        namebuilder.append(effect.hasFlicker() ?  ", "+LanguageHelper.translateToLocal("item.minecraft.firework_star.flicker", SignShopConfig.getPreferredLanguage())  : "");
                        namebuilder.append(effect.hasTrail()? ", "+LanguageHelper.translateToLocal("item.minecraft.firework_star.trail", SignShopConfig.getPreferredLanguage()) : "");
                    }
                }
                namebuilder.append(")");
                return namebuilder.toString();
            }
        }

        if(stack.getItemMeta().hasDisplayName())
            return getDisplayName(stack);
        return getDisplayName(stack);
    }

    public static void setMetaForID(ItemStack stack, Integer ID) {
        Map<String, String> metamap = new LinkedHashMap<String, String>();
        ItemMeta meta = stack.getItemMeta();
        SSDatabase db = new SSDatabase(filename);

        try {
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, ID);

            ResultSet setprops = (ResultSet)db.runStatement("SELECT PropertyName, ProperyValue FROM MetaProperty WHERE ItemMetaID = ?;", pars, true);
            if(setprops == null)
                return;
            try {
                while(setprops.next())
                    metamap.put(setprops.getString("PropertyName"), setprops.getString("ProperyValue"));
            } catch(SQLException ex) {
                return;
            }

            if(metamap.isEmpty())
                return;
        } finally {
            db.close();
        }


        if(!getPropValue("displayname", metamap).isEmpty())
            meta.setDisplayName(getPropValue("displayname", metamap));
        if(!getPropValue("lore", metamap).isEmpty()) {
            List<String> temp = Arrays.asList(getPropValue("lore", metamap).split(listSeperator));
            meta.setLore(temp);
        }
        if(!getPropValue("enchants", metamap).isEmpty()) {
            for(Map.Entry<Enchantment, Integer> enchant : signshopUtil.convertStringToEnchantments(getPropValue("enchants", metamap)).entrySet()) {
                meta.addEnchant(enchant.getKey(), enchant.getValue(), true);
            }
        }

        List<MetaType> metatypes = getTypesOfMeta(meta);

        try {
            for(MetaType type : metatypes) {
                if(type == MetaType.EnchantmentStorage) {
                    EnchantmentStorageMeta enchantmentmeta = (EnchantmentStorageMeta) meta;
                    if(!getPropValue("storedenchants", metamap).isEmpty()) {
                        for(Map.Entry<Enchantment, Integer> enchant : signshopUtil.convertStringToEnchantments(getPropValue("storedenchants", metamap)).entrySet()) {
                            enchantmentmeta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
                        }
                    }
                }
                else if(type == MetaType.LeatherArmor) {
                    LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                    if(!getPropValue("color", metamap).isEmpty())
                        leathermeta.setColor(Color.fromRGB(Integer.parseInt(getPropValue("color", metamap))));
                }
                else if(type == MetaType.Map) {
                    // We could set scaling here but for some reason Spigot doesn't when stacks are built up
                    // Which results in items not matching anymore if we do, so we won't
                }
                else if(type == MetaType.Repairable) {
                    Repairable repairmeta = (Repairable) meta;
                    if(!getPropValue("repaircost", metamap).isEmpty())
                        repairmeta.setRepairCost(Integer.parseInt(getPropValue("repaircost", metamap)));
                }
                else if(type == MetaType.Skull) {
                    SkullMeta skullmeta = (SkullMeta) meta;
                    if(!getPropValue("owner", metamap).isEmpty())
                        skullmeta.setOwner(getPropValue("owner", metamap));
                } else if(type == MetaType.Potion) {
                    PotionMeta potionmeta = (PotionMeta) meta;
                    List<PotionEffect> effects = convertStringToPotionMeta(getPropValue("potioneffects", metamap));
                    for(PotionEffect effect : effects) {
                        potionmeta.addCustomEffect(effect, true);
                    }
                } else if(type == MetaType.Fireworks) {
                    FireworkMeta fireworkmeta = (FireworkMeta) meta;
                    fireworkmeta.addEffects(convertStringToFireworkEffects(getPropValue("fireworkeffects", metamap)));
                    fireworkmeta.setPower(Integer.parseInt(getPropValue("fireworkpower", metamap)));
                }
            }
        } catch(ClassCastException ex) {

        } catch(NumberFormatException ex) {

        }


        stack.setItemMeta(meta);
    }

    public static Integer storeMeta(ItemStack stack) {
        if(!hasMeta(stack))
            return -1;

        SSDatabase db = new SSDatabase(filename);
        Map<String, String> metamap = getMetaAsMap(stack.getItemMeta());

        try {
            Integer existingID = getMetaID(stack, metamap);
            if(existingID > -1)
                return existingID;

            Integer itemmetaid;
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, metamap.hashCode());

            itemmetaid = (Integer)db.runStatement("INSERT INTO ItemMeta(ItemMetaHash) VALUES (?);", pars, false);

            if(itemmetaid == null || itemmetaid == -1)
                return -1;

            for(Map.Entry<String, String> metaproperty : metamap.entrySet()) {
                pars.clear();
                pars.put(1, itemmetaid);
                pars.put(2, metaproperty.getKey());
                pars.put(3, metaproperty.getValue());
                db.runStatement("INSERT INTO MetaProperty(ItemMetaID, PropertyName, ProperyValue) VALUES (?, ?, ?);", pars, false);
            }

            return itemmetaid;
        } finally {
            db.close();
        }
    }

    public static Integer getMetaID(ItemStack stack) {
        if(!hasMeta(stack))
            return -1;

        return getMetaID(stack, null);
    }

    private static Integer getMetaID(ItemStack stack, Map<String, String> pMetamap) {
        Map<String, String> metamap = (pMetamap != null ? pMetamap : getMetaAsMap(stack.getItemMeta()));
        SSDatabase db = new SSDatabase(filename);
        try {
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, metamap.hashCode());
            ResultSet set = (ResultSet)db.runStatement("SELECT ItemMetaID FROM ItemMeta WHERE ItemMetaHash = ?;", pars, true);
            if(set != null && set.next())
                return set.getInt("ItemMetaID");
        } catch (SQLException ex) {

        } finally {
            db.close();
        }

        return -1;
    }

    public static Map<String, String> getMetaAsMap(ItemMeta meta) {
        Map<String, String> metamap = new LinkedHashMap<String, String>();
        if(meta == null)
            return metamap;
        List<MetaType> types = getTypesOfMeta(meta);

        if(meta.getDisplayName() != null)
            metamap.put("displayname", meta.getDisplayName());
        if(meta.getEnchants() != null && !meta.getEnchants().isEmpty())
            metamap.put("enchants", signshopUtil.convertEnchantmentsToString(meta.getEnchants()));
        if(meta.getLore() != null && !meta.getLore().isEmpty()) {
            String lorearr[] = new String[meta.getLore().size()];
            metamap.put("lore", signshopUtil.implode(meta.getLore().toArray(lorearr), listSeperator));
        }

        for(MetaType type : types) {
            if(type == MetaType.EnchantmentStorage) {
                EnchantmentStorageMeta enchantmentmeta = (EnchantmentStorageMeta) meta;
                if(enchantmentmeta.hasStoredEnchants())
                    metamap.put("storedenchants", signshopUtil.convertEnchantmentsToString(enchantmentmeta.getStoredEnchants()));
            }
            else if(type == MetaType.LeatherArmor) {
                LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                metamap.put("color", Integer.toString(leathermeta.getColor().asRGB()));
            }
            else if(type == MetaType.Map) {
                MapMeta mapmeta = (MapMeta) meta;
                metamap.put("scaling", Boolean.toString(mapmeta.isScaling()));
            }
            else if(type == MetaType.Skull) {
                SkullMeta skullmeta = (SkullMeta) meta;
                if(skullmeta.hasOwner()) {
                    metamap.put("owner", skullmeta.getOwner());
                }
            }
            else if(type == MetaType.Repairable) {
                if(meta instanceof Repairable) {
                    Repairable repairmeta = (Repairable) meta;
                    if(repairmeta.hasRepairCost()) {
                        metamap.put("repaircost", Integer.toString(repairmeta.getRepairCost()));
                    }
                }
            } else if(type == MetaType.Potion) {
                PotionMeta potionmeta = (PotionMeta) meta;
                if(potionmeta.hasCustomEffects()) {
                    metamap.put("potioneffects", convertPotionMetaToString(potionmeta));
                }
            } else if(type == MetaType.Fireworks) {
                FireworkMeta fireworkmeta = (FireworkMeta) meta;
                if(fireworkmeta.hasEffects()) {
                    metamap.put("fireworkeffects", convertFireworkMetaToString(fireworkmeta));
                    metamap.put("fireworkpower", Integer.toString(fireworkmeta.getPower()));
                }
            }
        }

        return metamap;
    }

    private static List<MetaType> getTypesOfMeta(ItemMeta meta) {
        List<MetaType> types = new LinkedList<MetaType>();

        if(meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta)
            types.add(MetaType.EnchantmentStorage);
        if(meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta)
            types.add(MetaType.LeatherArmor);
        if(meta instanceof org.bukkit.inventory.meta.MapMeta)
            types.add(MetaType.Map);
        if(meta instanceof org.bukkit.inventory.meta.SkullMeta)
            types.add(MetaType.Skull);
        if(meta instanceof org.bukkit.inventory.meta.Repairable)
            types.add(MetaType.Repairable);
        if(meta instanceof org.bukkit.inventory.meta.PotionMeta)
            types.add(MetaType.Potion);
        if(meta instanceof org.bukkit.inventory.meta.FireworkMeta)
            types.add(MetaType.Fireworks);
        return types;
    }

    private static String getPropValue(String name, Map<String, String> metamap) {
        if(metamap.containsKey(name)) {
            return metamap.get(name);
        } else {
            return "";
        }
    }

    private static String convertPotionMetaToString(PotionMeta meta) {
        if(!meta.hasCustomEffects())
            return "";
        StringBuilder returnbuilder = new StringBuilder(meta.getCustomEffects().size() * 50);
        for(PotionEffect effect : meta.getCustomEffects()) {
            returnbuilder.append(effect.getType().getName());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Integer.toString(effect.getDuration()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Integer.toString(effect.getAmplifier()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Boolean.toString(effect.isAmbient()));
            returnbuilder.append(listSeperator);
        }
        return returnbuilder.toString();
    }

    @SuppressWarnings("deprecation") // Allowed for transition purposes
    private static List<PotionEffect> convertStringToPotionMeta(String meta) {
        List<PotionEffect> effects = new LinkedList<PotionEffect>();
        List<String> splitted = Arrays.asList(meta.split(listSeperator));
        if(splitted.isEmpty())
            return effects;
        for(String split : splitted) {
            String[] bits = split.split(valueSeperator);
            if(bits.length == 4) {
                try {
                    int dur = Integer.parseInt(bits[1]);
                    int amp = Integer.parseInt(bits[2]);
                    boolean amb = Boolean.parseBoolean(bits[3]);
                    PotionEffect effect = null;
                    try {
                        int id = Integer.parseInt(bits[0]);
                        effect = new PotionEffect(PotionEffectType.getById(id), dur, amp, amb);
                    } catch(NumberFormatException ex) {
                        PotionEffectType type = PotionEffectType.getByName(bits[0]);
                        if(type != null)
                            effect = new PotionEffect(PotionEffectType.getByName(bits[0]), dur, amp, amb);
                    }
                    if(effect != null)
                        effects.add(effect);
                } catch(NumberFormatException ex) {
                    continue;
                }


            }
        }

        return effects;
    }

    private static String getColorsAsAString(List<Color> colors) {
        List<String> temp = new LinkedList<String>();
        for(Color color : colors) {
            temp.add(Integer.toString(color.asRGB()));
        }
        String[] colorarr = new String[temp.size()];
        return signshopUtil.implode(temp.toArray(colorarr), innerListSeperator);
    }

    private static ImmutableList<Color> getColorsFromString(String colors) {
        List<Color> temp = new LinkedList<Color>();
        List<String> split = Arrays.asList(colors.split(innerListSeperator));
        for(String part : split) {
            try {
                temp.add(Color.fromRGB(Integer.parseInt(part)));
            } catch(NumberFormatException ex) {
                continue;
            }
        }

        return ImmutableList.copyOf(temp);
    }

    private static String convertFireworkMetaToString(FireworkMeta meta) {
        if(!meta.hasEffects())
            return "";
        StringBuilder returnbuilder = new StringBuilder(meta.getEffects().size() * 50);

        for(FireworkEffect effect : meta.getEffects()) {
            returnbuilder.append(effect.getType().toString());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(getColorsAsAString(effect.getColors()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(getColorsAsAString(effect.getFadeColors()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Boolean.toString(effect.hasFlicker()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Boolean.toString(effect.hasTrail()));
            returnbuilder.append(listSeperator);
        }
        return returnbuilder.toString();
    }

    private static List<FireworkEffect> convertStringToFireworkEffects(String meta) {
        List<FireworkEffect> effects = new LinkedList<FireworkEffect>();
        List<String> splitted = Arrays.asList(meta.split(listSeperator));
        if(splitted.isEmpty())
            return effects;
        for(String split : splitted) {
            String[] bits = split.split(valueSeperator);
            if(bits.length == 5) {
                try {
                    Builder builder = FireworkEffect.builder().with(FireworkEffect.Type.valueOf(bits[0]));
                    ImmutableList<Color> colors = getColorsFromString(bits[1]);
                    if(colors != null)
                        builder = builder.withColor(colors);
                    ImmutableList<Color> fadecolors = getColorsFromString(bits[2]);
                    if(fadecolors != null)
                        builder = builder.withFade(fadecolors);
                    builder = (Boolean.parseBoolean(bits[3]) ? builder.withFlicker() : builder);
                    builder = (Boolean.parseBoolean(bits[4]) ? builder.withTrail() : builder);

                    effects.add(builder.build());
                } catch(NumberFormatException ex) { }
            }
        }
        return effects;
    }

    private static enum MetaType {
        EnchantmentStorage,
        LeatherArmor,
        Map,
        Potion,
        Repairable,
        Fireworks,
        Skull,
        Stock,
    }
}
