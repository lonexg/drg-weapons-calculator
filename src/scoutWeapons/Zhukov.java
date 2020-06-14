package scoutWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

// Embedded Detonators does 5 damage per ammo (10/bullet) on reload
public class Zhukov extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Zhukov() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Zhukov(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Zhukov(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Zhukov Nuk17";
		weaponPic = WeaponPictures.zhukovs;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 11;
		carriedAmmo = 600;
		magazineSize = 50;  // Really 25
		rateOfFire = 30.0;  // Really 15
		reloadTime = 1.8;
		
		initializeModsAndOverclocks();
		// Grab initial values before customizing mods and overclocks
		setBaselineStats();
		
		// Selected Mods
		selectedTier1 = mod1;
		selectedTier2 = mod2;
		selectedTier3 = mod3;
		selectedTier4 = mod4;
		selectedTier5 = mod5;
		
		// Overclock slot
		selectedOverclock = overclock;
	}
	
	@Override
	protected void initializeModsAndOverclocks() {
		tier1 = new Mod[2];
		tier1[0] = new Mod("Expanded Ammo Bags", "+75 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("High Velocity Rounds", "+1 Direct Damage", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Magazine", "+10 Magazine Size", modIcons.magSize, 2, 0);
		tier2[1] = new Mod("Supercharged Feed Mechanism", "+8 Rate of Fire", modIcons.rateOfFire, 2, 1);
		tier2[2] = new Mod("Quickfire Ejector", "-0.6 Reload Time", modIcons.reloadSpeed, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Increased Caliber Rounds", "+2 Direct Damage", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Better Weight Balance", "x0.5 Base Spread", modIcons.baseSpread, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Blowthrough Rounds", "+1 Penetration", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "+30% Weakpoint Bonus", modIcons.weakpointBonus, 4, 1);
		tier4[2] = new Mod("Expanded Ammo Bags", "+150 Max Ammo", modIcons.carriedAmmo, 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Conductive Bullets", "+30% Direct Damage dealt to enemies either being Electrocuted or affected by Scout's IFG grenade", modIcons.electricity, 5, 0);
		tier5[1] = new Mod("Get In, Get Out", "+100% Movement Speed for 2 seconds after reloading an empty magazine", modIcons.movespeed, 5, 1);
		
		overclocks = new Overclock[5];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Minimal Magazines", "+2 Rate of Fire, -0.4 Reload Time", overclockIcons.reloadSpeed, 0);
		overclocks[1] = new Overclock(Overclock.classification.balanced, "Custom Casings", "+30 Mag Size, -1 Direct Damage", overclockIcons.magSize, 1);
		overclocks[2] = new Overclock(Overclock.classification.unstable, "Cryo Minelets", "Any bullets that impact terrain get converted to Cryo Minelets. After 1 second of arming time they will explode on any "
				+ "enemies that get close, dealing 10 Cold Damage each. They automatically explode after 3 seconds. -1 Direct Damage, -10 Magazine Size", overclockIcons.coldDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Embedded Detonators", "Bullets that deal damage to an enemy's healthbar leave behind a detonator that deals 10 Area Damage to the enemy "
				+ "upon reloading. -3 Direct Damage, -75 Max Ammo.", overclockIcons.specialReload, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Gas Recycling", "+5 Direct Damage, but it can no longer gain bonus damage from hitting a Weakpoint. Additionally, x1.5 Base Spread "
				+ "and -50% Movement Speed while firing.", overclockIcons.directDamage, 4);
	}
	
	@Override
	public void buildFromCombination(String combination) {
		boolean combinationIsValid = true;
		char[] symbols = combination.toCharArray();
		if (combination.length() != 6) {
			System.out.println(combination + " does not have 6 characters, which makes it invalid");
			combinationIsValid = false;
		}
		else {
			List<Character> validModSymbols = Arrays.asList(new Character[] {'A', 'B', 'C', '-'});
			for (int i = 0; i < 5; i ++) {
				if (!validModSymbols.contains(symbols[i])) {
					System.out.println("Symbol #" + (i+1) + ", " + symbols[i] + ", is not a capital letter between A-C or a hyphen");
					combinationIsValid = false;
				}
			}
			if (symbols[0] == 'C') {
				System.out.println("Zhukov's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Zhukov's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Zhukov's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-5 or a hyphen");
				combinationIsValid = false;
			}
		}
		
		if (combinationIsValid) {
			switch (symbols[0]) {
				case '-': {
					selectedTier1 = -1;
					break;
				}
				case 'A': {
					selectedTier1 = 0;
					break;
				}
				case 'B': {
					selectedTier1 = 1;
					break;
				}
			}
			
			switch (symbols[1]) {
				case '-': {
					selectedTier2 = -1;
					break;
				}
				case 'A': {
					selectedTier2 = 0;
					break;
				}
				case 'B': {
					selectedTier2 = 1;
					break;
				}
				case 'C': {
					selectedTier2 = 2;
					break;
				}
			}
			
			switch (symbols[2]) {
				case '-': {
					selectedTier3 = -1;
					break;
				}
				case 'A': {
					selectedTier3 = 0;
					break;
				}
				case 'B': {
					selectedTier3 = 1;
					break;
				}
			}
			
			switch (symbols[3]) {
				case '-': {
					selectedTier4 = -1;
					break;
				}
				case 'A': {
					selectedTier4 = 0;
					break;
				}
				case 'B': {
					selectedTier4 = 1;
					break;
				}
				case 'C': {
					selectedTier4 = 2;
					break;
				}
			}
			
			switch (symbols[4]) {
				case '-': {
					selectedTier5 = -1;
					break;
				}
				case 'A': {
					selectedTier5 = 0;
					break;
				}
				case 'B': {
					selectedTier5 = 1;
					break;
				}
			}
			
			switch (symbols[5]) {
				case '-': {
					selectedOverclock = -1;
					break;
				}
				case '1': {
					selectedOverclock = 0;
					break;
				}
				case '2': {
					selectedOverclock = 1;
					break;
				}
				case '3': {
					selectedOverclock = 2;
					break;
				}
				case '4': {
					selectedOverclock = 3;
					break;
				}
				case '5': {
					selectedOverclock = 4;
					break;
				}
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public Zhukov clone() {
		return new Zhukov(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "Zhukov";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.zhukovsGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 1) {
			toReturn += 1;
		}
		if (selectedTier3 == 0) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 1 || selectedOverclock == 2) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 3;
		}
		else if (selectedOverclock == 4) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private int getAreaDamage() {
		// Equipping the Overclock "Embedded Detonators" leaves a detonator inside enemies that does 5 Area Damage per Ammo (10/Bullet) that deals damage to an enemy upon reloading the Zhukovs
		if (selectedOverclock == 3) {
			return 10;
		}
		else { 
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 75;
		}
		if (selectedTier4 == 2) {
			toReturn += 150;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 75;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier2 == 0) {
			toReturn += 10;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 30;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 10;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 1) {
			toReturn += 8.0;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 2) {
			toReturn -= 0.6;
		}
		
		if (selectedOverclock == 0) {
			toReturn -= 0.4;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getWeakpointBonus() {
		if (selectedOverclock == 4) {
			// Since this removes the Zhukov's ability to get weakpoint bonus damage, return a -100% to symbolize it.
			return -1.0;
		}
		else if (selectedTier4 == 1){
			return 0.3;
		}
		else {
			return 0;
		}
	}
	private double getMovespeedWhileFiring() {
		double modifier = 1.0;
		
		if (selectedOverclock == 4) {
			modifier -= 0.5;
		}
		
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[10];
		
		boolean directDamageModified = selectedTier1 == 1 || selectedTier3 == 0 || (selectedOverclock > 0 && selectedOverclock < 5);
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		// This stat only applies to OC "Embedded Detonators"
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), selectedOverclock == 3, selectedOverclock == 3);
		
		boolean magSizeModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 2;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedTier4 == 2 || selectedOverclock == 3;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedTier2 == 1 || selectedOverclock == 0);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), selectedTier2 == 2 || selectedOverclock == 0);
		
		String sign = "";
		if (selectedOverclock != 4) {
			sign = "+";
		}
		
		boolean weakpointModified = selectedTier4 == 1 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Weakpoint Bonus:", sign + convertDoubleToPercentage(getWeakpointBonus()), weakpointModified, weakpointModified);
		
		toReturn[7] = new StatsRow("Max Penetrations:", getMaxPenetrations(), selectedTier4 == 0, selectedTier4 == 0);
		
		boolean baseSpreadModified = selectedTier3 == 1 || selectedOverclock == 4;
		toReturn[8] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified, baseSpreadModified);
		
		toReturn[9] = new StatsRow("Movespeed While Firing: (m/sec)", getMovespeedWhileFiring(), selectedOverclock == 4, selectedOverclock == 4);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// This weapon can never deal splash damage
		return false;
	}
	
	private double calculateAvgNumBulletsNeededToFreeze() {
		// Minelets do 10 Cold Damage upon detonation, but they have to take 1 second to arm first.
		// While Frozen, bullets do x3 Direct Damage.
		double effectiveRoF = getRateOfFire() / 2.0;
		double timeToFreeze = EnemyInformation.averageTimeToFreeze(-10, effectiveRoF);
		return Math.ceil(timeToFreeze * effectiveRoF);
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		// TODO: both overlaps between (IFG and Conductive Bullets) and (Cryo Minelets and Frozen) are clunky. This method should be refactored in the future.
		double generalAccuracy, duration;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		double effectiveMagazineSize = getMagazineSize() / 2;
		double effectiveRoF = getRateOfFire() / 2.0;
		if (burst) {
			duration = effectiveMagazineSize / effectiveRoF;
		}
		else {
			duration = effectiveMagazineSize / effectiveRoF + getReloadTime();
		}
		
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
			areaDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		// Conductive Bullets is x1.3 multiplier on Electrocuted targets or targets inside IFG field
		if (selectedTier5 == 0 && (statusEffects[2] || statusEffects[3])) {
			directDamage *= 1.3;
		}
		
		double damagePerMagazine;
		int bulletsThatHitTarget;
		if (selectedOverclock == 2) {
			// Is the primary target already frozen?
			if (statusEffects[1]) {
				// If this is the case, then the Frozen x3 damage has already been applied.
				bulletsThatHitTarget = (int) Math.round(effectiveMagazineSize * generalAccuracy);
			}
			else {
				// First, you have to intentionally miss bullets in order to convert them to Cryo Minelets, then wait 1 second, and unload the rest of the clip into
				// the now-frozen enemy for x3 damage. Damage vs frozen enemies does NOT benefit from weakpoint damage on top of the frozen multiplier.
				duration += 1;
				double numBulletsMissedToBecomeCryoMinelets = calculateAvgNumBulletsNeededToFreeze();
				directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
				bulletsThatHitTarget = (int) Math.round((effectiveMagazineSize - numBulletsMissedToBecomeCryoMinelets) * generalAccuracy);
			}
			
			damagePerMagazine = directDamage * bulletsThatHitTarget;
		}
		else {
			if (weakpoint && selectedOverclock != 4 && !statusEffects[1]) {
				double weakpointAccuracy = estimatedAccuracy(true) / 100.0;
				int bulletsThatHitWeakpoint = (int) Math.round(effectiveMagazineSize * weakpointAccuracy);
				bulletsThatHitTarget = (int) Math.round(effectiveMagazineSize * generalAccuracy) - bulletsThatHitWeakpoint;
				damagePerMagazine = bulletsThatHitWeakpoint * increaseBulletDamageForWeakpoints2(directDamage, getWeakpointBonus()) + bulletsThatHitTarget * directDamage + (bulletsThatHitWeakpoint + bulletsThatHitTarget) * areaDamage;
			}
			else {
				bulletsThatHitTarget = (int) Math.round(effectiveMagazineSize * generalAccuracy);
				damagePerMagazine = (directDamage + areaDamage) * bulletsThatHitTarget;
			}
		}
		
		return damagePerMagazine / duration;
	}
	

	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(false, false, false);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(false, false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateSingleTargetDPS(false, true, true);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 0) {
			return calculateSingleTargetDPS(false, false, false);
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		// If there's an odd number carried ammo, round up since you can fire the last "odd" ammo as a full-damage shot
		double effectiveCarriedAmmo = Math.ceil(getCarriedAmmo() / 2.0);
		
		if (selectedOverclock == 2) {
			double bulletsIntentionallyMissedPerMag = calculateAvgNumBulletsNeededToFreeze();
			double numMags = numMagazines((int) effectiveCarriedAmmo, (int) effectiveMagazineSize);
			double totalBulletsIntentionallyWasted = Math.round(bulletsIntentionallyMissedPerMag * numMags);
			
			return (effectiveMagazineSize + effectiveCarriedAmmo - totalBulletsIntentionallyWasted) * (getDirectDamage() * UtilityInformation.Frozen_Damage_Multiplier) * calculateMaxNumTargets();
		}
		else {
			// Area Damage only applies when using OC "Embedded Detonators", so it doesn't need to be modeled for the Cryo Minelets' max damage.
			return (effectiveMagazineSize + effectiveCarriedAmmo) * (getDirectDamage() + getAreaDamage()) * calculateMaxNumTargets();
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getMaxPenetrations();
	}

	@Override
	public double calculateFiringDuration() {
		// Because of how this weapon works, all these numbers need to be halved to be accurate.
		int effectiveMagazineSize = getMagazineSize() / 2;
		// If there's an odd number carried ammo, round up since you can fire the last "odd" ammo as a full-damage shot
		int effectiveCarriedAmmo = (int) Math.ceil(((double) getCarriedAmmo()) / 2.0);
		double effectiveRoF = getRateOfFire() / 2.0;
		
		double timeToFireMagazine = ((double) effectiveMagazineSize) / effectiveRoF;
		return numMagazines(effectiveCarriedAmmo, effectiveMagazineSize) * timeToFireMagazine + numReloads(effectiveCarriedAmmo, effectiveMagazineSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		// Because the Overclock "Gas Recycling" removes the ability to get any weakpoint bonus damage, that has to be modeled here.
		double dmgPerShot;
		if (selectedOverclock == 4) {
			dmgPerShot = getDirectDamage();
		}
		else {
			dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus());
		}
		
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double unchangingWidth = 14;
		double changingWidth = 384;
		
		double crosshairHeightPixels = 98;
		double crosshairWidthPixels = unchangingWidth + changingWidth * getBaseSpread();
		
		return AccuracyEstimator.calculateRectangularAccuracy(weakpointAccuracy, true, crosshairWidthPixels, crosshairHeightPixels);
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			getDirectDamage(),  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			getAreaDamage(),  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] DoTDamage = {
			0,  // Fire
			0,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, getWeakpointBonus(), 0.0, 0.0);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// OC "Gas Recycling" reduces Scout's movement speed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Mod Tier 5 "Get In, Get Out" gives 100% movement speed increase for 2 sec after reloading empty clips
		if (selectedTier5 == 1) {
			// Because this buff lasts 2 seconds, but I don't think it's possible to have 100% uptime. Use the uptime as a coefficient to reduce the value of the movespeed buff.
			double effectiveMagazineSize = getMagazineSize() / 2.0;
			double effectiveRoF = getRateOfFire() / 2.0;
			double timeToFireMagazineAndReload = (effectiveMagazineSize / effectiveRoF) + getReloadTime();
			
			// Just because I don't think it's possible doesn't mean I'm not safeguarding against it.
			double uptimeCoefficient = Math.min(2.0 / timeToFireMagazineAndReload, 1);
			
			utilityScores[0] += uptimeCoefficient * DwarfInformation.walkSpeed * UtilityInformation.Movespeed_Utility;
		}
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage()) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Cryo Minelets" applies Cryo damage to missed bullets
		if (selectedOverclock == 2) {
			// Cryo minelets: 1 placed per 2 ammo, minelets arm in 1 second, and detonate in 3 seconds if no enemy is around.
			// Minelets seem to do 10 Cold Damage each, and they don't explode in a radius -- instead it seems that they spurt off in a random direction for 2.5m.
			int estimatedNumTargetsSlowedOrFrozen = 3;  // This is a pure, unadulterated guess.
			
			utilityScores[3] = estimatedNumTargetsSlowedOrFrozen * UtilityInformation.Cold_Utility;
			utilityScores[6] = estimatedNumTargetsSlowedOrFrozen * UtilityInformation.Frozen_Utility;
		}
		else {
			utilityScores[3] = 0;
			utilityScores[6] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		double effectiveMagazineSize = getMagazineSize() / 2.0;
		return effectiveMagazineSize * (getDirectDamage() + getAreaDamage()) * calculateMaxNumTargets();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', " + DatabaseConstants.patchNumberID + ");";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.add(String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[1].getText(), ""));
		
		// Tier 2
		toReturn.add(String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 18, 0, 0, 0, 12, 0, tier2[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 0, 0, 12, 0, 18, tier2[1].getText(), ""));
		toReturn.add(String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[2].getText(), ""));
		
		// Tier 3
		toReturn.add(String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 30, 0, 20, tier3[1].getText(), ""));
		
		// Tier 4
		toReturn.add(String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 25, 0, 0, 15, 0, tier4[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 0, 0, 36, 25, 15, 0, tier4[1].getText(), ""));
		toReturn.add(String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 3800, 25, 36, 0, 0, 0, 15, tier4[2].getText(), ""));
		
		// Tier 5
		toReturn.add(String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 0, 0, 60, 40, 0, 110, tier5[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 40, 60, 0, 0, 110, 0, tier5[1].getText(), ""));
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', " + DatabaseConstants.patchNumberID + ");";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.add(String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8450, 0, 130, 0, 100, 0, 70, overclocks[0].getText(), ""));
		
		// Balanced
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7700, 0, 95, 0, 75, 140, 0, overclocks[1].getText(), ""));
		
		// Unstable
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7300, 135, 0, 95, 65, 0, 0, overclocks[2].getText(), ""));
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7550, 65, 0, 90, 0, 0, 135, overclocks[3].getText(), ""));
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7800, 125, 0, 0, 0, 70, 105, overclocks[4].getText(), ""));
		
		return toReturn;
	}
}
