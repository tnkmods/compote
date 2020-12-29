# Compote

Adds various quality of life improvements to the vanilla minecraft composter. All changes (except for JEI integration) are configurable in the `compote-common.toml` file.

## Features

__JEI Integration__

See what is compostable, and what the chances of raising a composter's level are for every compostable item.

__Data-Driven__

Adding, removing or changing compostable items can all be done with custom `compote:composting` recipes, so datapack or modpack developers can customize to their needs. 

## Tweaks

__Right Click To Empty__

Shift right click with an empty hand to empty the composter [_Off By Default_]

__Customizable Levels__

By default the vanilla composter requires 7 levels of progress before generating a piece of bonemeal. Compote lets you set it to as little at 1 level. [_Set to 7 by Default_]

__Insert & Extract From Any Face__

Enable insertion and extraction from any face of the composter, instead of just top and bottom in vanilla. [_Both set to off by default_]

__Turn Off First Compost Success__

By default, vanilla will always make the first composting attempt successful (raising it by a level regardless of chance). You can turn this off if you want [_Vanilla behavior by default_]


## Custom Recipes

The format is as follows:

```
{
  "type": "compote:composting",
  "add": [
    {
      "item": "minecraft:nether_star",
      "chance": 1.0
    }
  ],
  "remove": [
    {
      "item": "minecraft:wheat_seeds"
    }
  ],
  "change": [
    {
      "item": "minecraft:pumpkin",
      "chance": 1.0
  ]
}
```
