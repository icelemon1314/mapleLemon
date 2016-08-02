  package server;
  
import constants.BattleConstants;
  import constants.GameConstants;
  import java.util.ArrayList;
  import java.util.Collections;
  import java.util.List;
  
  public class RandomRewards
  {
    private static List<Integer> compiledGold = null;
    private static List<Integer> compiledSilver = null;
    private static List<Integer> compiledPeanut = null;
    private static List<Integer> compiledEvent = null;
    private static List<Integer> compiledEventC = null;
    private static List<Integer> compiledEventB = null;
    private static List<Integer> compiledEventA = null;
    private static List<Integer> compiledPokemon = null;
    private static List<Integer> compiledDrops = null;
    private static List<Integer> compiledDropsB = null;
    private static List<Integer> compiledDropsA = null;
    private static List<Integer> tenPercent = null;
  
    private static void processRewards(List<Integer> returnArray, int[] list)
    {
      int lastitem = 0;
      for (int i = 0; i < list.length; i++) {
        if (i % 2 == 0) {
            lastitem = list[i];
        } else {
          for (int j = 0; j < list[i]; j++) {
            returnArray.add(lastitem);
          }
        }
      }
      Collections.shuffle(returnArray);
    }
  
    private static void processRewardsSimple(List<Integer> returnArray, int[] list) {
      for (int i = 0; i < list.length; i++) {
        returnArray.add(list[i]);
      }
      Collections.shuffle(returnArray);
    }
  
    private static void processPokemon(List<Integer> returnArray, BattleConstants.PItem[] list) {
        for (BattleConstants.PItem lastitem : list) {
            for (int j = 0; j < lastitem.getItemChance(); j++) {
                returnArray.add(lastitem.getId());
            }
        }
      Collections.shuffle(returnArray);
    }
  
    public static int getGoldBoxReward() {
      return (compiledGold.get(Randomizer.nextInt(compiledGold.size())));
    }
  
    public static int getSilverBoxReward() {
      return (compiledSilver.get(Randomizer.nextInt(compiledSilver.size())));
    }
  
    public static int getPeanutReward() {
      return (compiledPeanut.get(Randomizer.nextInt(compiledPeanut.size())));
    }
  
    public static int getPokemonReward() {
      return (compiledPokemon.get(Randomizer.nextInt(compiledPokemon.size())));
    }
  
    public static int getEventReward() {
      int chance = Randomizer.nextInt(101);
      if (chance < 66) {
          return (compiledEventC.get(Randomizer.nextInt(compiledEventC.size())));
      }
      if (chance < 86) {
          return (compiledEventB.get(Randomizer.nextInt(compiledEventB.size())));
      }
      if (chance < 96) {
        return (compiledEventA.get(Randomizer.nextInt(compiledEventA.size())));
      }
      return (compiledEvent.get(Randomizer.nextInt(compiledEvent.size())));
    }
  
    public static int getDropReward()
    {
      int chance = Randomizer.nextInt(101);
      if (chance < 76) {
          return (compiledDrops.get(Randomizer.nextInt(compiledDrops.size())));
      }
      if (chance < 96) {
        return (compiledDropsB.get(Randomizer.nextInt(compiledDropsB.size())));
      }
      return (compiledDropsA.get(Randomizer.nextInt(compiledDropsA.size())));
    }
  
    public static List<Integer> getTenPercent()
    {
      return tenPercent;
    }
  
    static void load()
    {
    }
  
    static
    {
      List returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.goldrewards);
  
      compiledGold = returnArray;
  
      returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.silverrewards);
  
      compiledSilver = returnArray;
  
      returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.fishingReward);
  
      returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.eventCommonReward);
  
      compiledEventC = returnArray;
  
      returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.eventUncommonReward);
  
      compiledEventB = returnArray;
  
      returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.eventRareReward);
      processRewardsSimple(returnArray, GameConstants.tenPercent);
      processRewardsSimple(returnArray, GameConstants.tenPercent);
  
      compiledEventA = returnArray;
  
      returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.eventSuperReward);
  
      compiledEvent = returnArray;
  
      returnArray = new ArrayList();
  
      processRewards(returnArray, GameConstants.peanuts);
  
      compiledPeanut = returnArray;
  
      returnArray = new ArrayList();
  
      processPokemon(returnArray, BattleConstants.PokemonItem.values());
      processPokemon(returnArray, BattleConstants.HoldItem.values());
  
      compiledPokemon = returnArray;
  
      returnArray = new ArrayList();
  
      processRewardsSimple(returnArray, GameConstants.normalDrops);
  
      compiledDrops = returnArray;
  
      returnArray = new ArrayList();
  
      processRewardsSimple(returnArray, GameConstants.rareDrops);
  
      compiledDropsB = returnArray;
  
      returnArray = new ArrayList();
  
      processRewardsSimple(returnArray, GameConstants.superDrops);
  
      compiledDropsA = returnArray;
  
      returnArray = new ArrayList();
  
      processRewardsSimple(returnArray, GameConstants.tenPercent);
  
      tenPercent = returnArray;
    }
  }

