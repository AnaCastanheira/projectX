package org.academiadecodigo.bootcamp.dungeons.game;
import org.academiadecodigo.bootcamp.dungeons.Randomizer;
import org.academiadecodigo.bootcamp.dungeons.character.ReturningAttackValues;
import org.academiadecodigo.bootcamp.dungeons.character.enemy.EnemyFactory;
import org.academiadecodigo.bootcamp.dungeons.character.player.PlayerClasses;
import org.academiadecodigo.bootcamp.dungeons.character.player.Player;
import org.academiadecodigo.bootcamp.dungeons.character.enemy.Enemy;
import org.academiadecodigo.bootcamp.dungeons.character.player.items.ItemTypes;
import org.academiadecodigo.bootcamp.dungeons.character.player.items.WeaponTypes;
import org.academiadecodigo.bootcamp.dungeons.game.sounds.GameSounds;

public class Game {

    private static final int MANA_POTION_DROP_CHANCE = 20;
    private static final int HEALTH_POTION_DROP_CHANCE = 20;
    private static final int WEAPON_DROP_CHANCE = 10;

    private Player player;
    private Enemy enemy;
    private int currentLevel;
    private int skillIndex1;
    private int skillIndex2;
    private int weaponIndex;
    Images images;

    boolean characterChosen;
    boolean gameStarted;
    boolean outOfCombat;
    boolean choosingSkill;
    boolean choosingWeapon;


    private boolean gotLoot;
    private boolean gotWeapon;
    private boolean choseSkill;

    private boolean bossAppeared;


    public Game(Images images){

        GameSounds.gameMusic.play(true);
        GameSounds.gameMusic.setLoop(1000);

        this.images = images;

        images.initialImage();
    }


    void init(){

        gameStarted = true;
        images.deleteInitialImage();
        outOfCombat = true;
        currentLevel = 1;
        images.backgound();
        images.choosePlayer();

    }


    void createPlayer(PlayerClasses playerClass){

        GameSounds.gameStart.play(true);

        player = new Player(playerClass);
        characterChosen = true;

        images.healthMana();
         images.healthManaText(player.getHealthPoints(),player.getMaxHealthPoints(),
                player.getManaPoints(), player.getMaxManaPoints());

        images.storyMenu();
    }




    private void enemyTurn(){
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GameSounds.enemyAttack.play(true);
        choosingWeapon = false;
        System.out.println("Enemy attacking");

        player.calculateDamageTaken(enemy.attack());

        if (player.getHealthPoints() <= 0){
            gameOver();
            return;
        }

        images.healthManaText(player.getHealthPoints(),player.getMaxHealthPoints(),
                player.getManaPoints(), player.getMaxManaPoints());
    }


    void playerAttack(){

        GameSounds.playerWeaponAttack.play(true);

        enemy.calculateDamageTaken(player.attack());
        images.enemyHealtText(enemy.getHealthPoints());

        if (enemy.getHealthPoints() <= 0){

            generateLoot(enemy.getExperience());

            return;
        }

        enemyTurn();
    }


    void playerChooseSkill(int choice){

        if (choice == 1){
            images.textStory("You chose " + player.getPlayerPossibleSpellsList().get(skillIndex1).toString());
            player.chooseSkill(skillIndex1, skillIndex2);
            choosingSkill = false;
            createEnemy();
            return;
        }

        images.textStory("You chose " + player.getPlayerPossibleSpellsList().get(skillIndex2).toString());
        player.chooseSkill(skillIndex2, skillIndex1);

        choosingSkill = false;
    }


    private void getTwoRandomSkills(){

        skillIndex1 = Randomizer.randomizeBetween(0, player.getPlayerPossibleSpellsList().size() -1);
        skillIndex2 = skillIndex1;

        while (skillIndex1 == skillIndex2){
            skillIndex2 = Randomizer.randomizeBetween(0, player.getPlayerPossibleSpellsList().size() -1);
        }

        images.chooseSkillMenu();

        images.textStory(
                "Press K to take " , player.getPlayerPossibleSpellsList().get(skillIndex1).toString(),
        "Press L to take " , player.getPlayerPossibleSpellsList().get(skillIndex2).toString() );
        choseSkill = true;
        images.deleteAfterBattleMenu();
    }



    void playerRest(){
        outOfCombat = false;
        GameSounds.restSound.play(true);
        images.deleteAfterBattleMenu();
        images.deleteBattleMenu();

        if (gotLoot){
            images.deleteGeneratedLoot();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!player.rest()){

            GameSounds.ambushSound.play(true);
            enemy = EnemyFactory.createEliteEnemy();
            GameSounds.enemyAppears.play(true);     // TODO: 15/06/2019 change to unique sound
            images.enemy(enemy.getEnemyTypes());

            images.battleMenu();

            images.enemyHealth();

            if (choseSkill){
                choseSkill = false;
                images.deleteChooseSkillMenu();
            }

            enemyTurn();
            return;
        }

        createEnemy();
        images.healthManaText(player.getHealthPoints(),player.getMaxHealthPoints(),
                player.getManaPoints(), player.getMaxManaPoints());
    }


    void playerUseHealthPotion(){

        if (player.useHealthPotion()){
            images.textStory("Used Health Potion");

            GameSounds.drinkPotion.play(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            images.healthManaText(player.getHealthPoints(),player.getMaxHealthPoints(),
                    player.getManaPoints(), player.getMaxManaPoints());
            enemyTurn();
            return;
        }

        images.textStory("You don't have any");
        GameSounds.wrongMenuChoice.play(true);
    }


    void playerUseManaPotion(){

        if (player.useManaPotion()){
            images.textStory("Used Mana Potion");

            GameSounds.drinkPotion.play(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            images.healthManaText(player.getHealthPoints(),player.getMaxHealthPoints(),
                    player.getManaPoints(), player.getMaxManaPoints());
            enemyTurn();
            return;
        }

        images.textStory("You don't have any");
        GameSounds.wrongMenuChoice.play(true);
    }

    void playerFlee(){

        if (player.flee()){
            outOfCombat = true;

            images.deleteEnemy();
            images.deleteBattleMenu();
            images.afterBattleMenu();
            images.deleteEnemyHealtText();
            images.deleteEnemyHealth();

            images.textStory("You flee successfully");
            return;
        }

        images.textStory("You fail to flee");
        enemyTurn();
    }


    void playerUseSkill(int skillNumber) {

        ReturningAttackValues damage = player.castSpell(skillNumber,images);

        if (damage != null){

            GameSounds.playerSkillAttack.play(true);

            enemy.calculateDamageTaken(damage);
            images.enemyHealtText(enemy.getHealthPoints());

            if (enemy.getHealthPoints() <= 0){

                generateLoot(enemy.getExperience());

                return;
            }

            enemyTurn();
            return;

        }

        GameSounds.wrongMenuChoice.play(true);
    }



    void start() {


        images.afterBattleMenu();

        createEnemy();
        images.player(player.getPlayerClass());
    }


    void createEnemy(){

        if (gotLoot){
            images.deleteGeneratedLoot();
        }

        if (gotWeapon){
            images.deleteChangeWeaponMenu();
        }

        enemy = EnemyFactory.createEnemy();

        if (player.getPlayerLevel() >= 5){
            bossAppeared = true;
            enemy = EnemyFactory.createBoss();
        }

        if (choseSkill){
            choseSkill = false;
            images.deleteChooseSkillMenu();
        }

        GameSounds.enemyAppears.play(true);

        images.deleteAfterBattleMenu();
        images.battleMenu();

        images.enemy(enemy.getEnemyTypes());
        outOfCombat = false;

        images.enemyHealth();
        images.enemyHealtText(enemy.getHealthPoints());

        images.textStory("A " + enemy.getEnemyName() + " appears");
    }


    private void generateLoot(int experience){

        images.deleteEnemyHealth();
        images.deleteEnemyHealtText();

        gotLoot = false;
        gotWeapon = false;

        images.deleteBattleMenu();
        images.deleteEnemy();
        images.afterBattleMenu();

        GameSounds.victorySound.play(true);

        outOfCombat = true;


        if (bossAppeared){

            win();
            return;
        }

        player.gainExperience(experience);

        System.out.println("Enemy dead\nGained " + enemy.getExperience() + " experience");

        if (Randomizer.getPercentage() <= MANA_POTION_DROP_CHANCE){
            images.textStory("You got a Mana Potion");
            player.addManaPotion();
            gotLoot = true;
            images.lootGenerated(ItemTypes.MANAPOTION);
        }

        if (Randomizer.getPercentage() <= HEALTH_POTION_DROP_CHANCE && !gotLoot){
            images.textStory("You got a Health Potion");
            player.addHealthPotion();
            gotLoot = true;
            images.lootGenerated(ItemTypes.HEALTHPOTION);
        }

        if (Randomizer.getPercentage() <= WEAPON_DROP_CHANCE && !gotLoot){
            generateWeapon();
            images.lootGenerated(WeaponTypes.values()[weaponIndex]);
            images.changeWeaponMenu();
            gotLoot = true;
            gotWeapon = true;
        }


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (currentLevel < player.getPlayerLevel() && player.getPlayerLevel() <= 4){

            choosingSkill = true;
            currentLevel++;
            getTwoRandomSkills();
        }
    }


    private void gameOver(){
        System.out.println("You died on level " + player.getPlayerLevel());

        GameSounds.enemyWins.play(true);

        images.deletePlayer();
        images.deleteBattleMenu();
        images.deleteEnemy();

        images.gameOver();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }


    private void generateWeapon(){

        choosingWeapon = true;
        weaponIndex = Randomizer.randomizeBetween(0, WeaponTypes.values().length -1);

        images.textStory("The enemy dropped a ",  WeaponTypes.values()[weaponIndex].toString(),
                "Press Y to switch from ", player.getWeapon().toString());
    }

    void playerSwitchWeapon(){

        player.setWeapon(weaponIndex);

        images.textStory("You now have a " + WeaponTypes.values()[weaponIndex]);
        choosingWeapon = false;
        images.deleteChangeWeaponMenu();
    }


    private void win(){

        GameSounds.finalVictory.play(true);
        images.deletePlayer();
        images.credits();
        gameStarted = false;
        characterChosen = false;
        outOfCombat = true;

        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

       // images.initialImage();
    }
}
