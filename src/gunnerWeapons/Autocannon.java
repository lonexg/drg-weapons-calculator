package gunnerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

public class Autocannon extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int areaDamage;
	private double aoeRadius;
	private int magazineSize;
	private int carriedAmmo;
	private double movespeedWhileFiring;
	private double increaseScalingRate;
	private double minRateOfFire;
	private double maxRateOfFire;
	private double reloadTime;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Autocannon() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Autocannon(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Autocannon(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Thunderhead\" Heavy Autocannon";
		weaponPic = WeaponPictures.autocannon;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 14;
		areaDamage = 9;
		aoeRadius = 1.4;  // meters
		magazineSize = 110;
		carriedAmmo = 440;
		movespeedWhileFiring = 0.5;
		increaseScalingRate = 0.3;
		minRateOfFire = 3.0;
		maxRateOfFire = 5.5;
		reloadTime = 5.0;  // seconds
		
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
		tier1 = new Mod[3];
		tier1[0] = new Mod("Increased Caliber Rounds", "+3 Direct Damage", modIcons.directDamage, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "+110 Magazine Size", modIcons.magSize, 1, 1);
		tier1[2] = new Mod("Expanded Ammo Bags", "+220 Max Ammo", modIcons.carriedAmmo, 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Tighter Barrel Alignment", "-30% Base Spread", modIcons.baseSpread, 2, 0);
		tier2[1] = new Mod("Improved Gas System", "+0.2 Min Rate of Fire, +1.5 Max Rate of Fire", modIcons.rateOfFire, 2, 1);
		tier2[2] = new Mod("Lighter Barrel Assembly", "+1 Min Rate of Fire, x2 RoF Scaling Rate", modIcons.rateOfFire, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Supercharged Feed Mechanism", "+0.6 Min Rate of Fire, +2 Max Rate of Fire", modIcons.rateOfFire, 3, 0);
		tier3[1] = new Mod("Loaded Rounds", "+2 Area Damage", modIcons.areaDamage, 3, 1);
		tier3[2] = new Mod("High Velocity Rounds", "+4 Direct Damage", modIcons.directDamage, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Penetrating Rounds", "+400% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Shrapnel Rounds", "+0.6m AoE Radius", modIcons.aoeRadius, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Feedback Loop", "x1.2 Direct and Area Damage when at Max Rate of Fire", modIcons.directDamage, 5, 0);
		tier5[1] = new Mod("Suppressive Fire", "50% chance to inflict Fear to enemies within a 1m radius on impact.", modIcons.fear, 5, 1);
		tier5[2] = new Mod("Damage Resistance At Full RoF", "33% Damage Resistance when at Max Rate of Fire", modIcons.damageResistance, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Composite Drums", "+110 Max Ammo, -0.5 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Splintering Shells", "+1 Area Damage, +0.3m AoE Radius", overclockIcons.aoeRadius, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Carpet Bomber", "+3 Area Damage, +0.7m AoE Radius, -6 Direct Damage", overclockIcons.areaDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Combat Mobility", "Increases movement speed while using from 50% to 65% of normal walk speed, -2 Direct Damage", overclockIcons.movespeed, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Big Bertha", "+12 Direct Damage, -30% Base Spread, x0.5 Magazine Size, -110 Max Ammo, -1.5 Max Rate of Fire", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Neurotoxin Payload", "30% Chance to inflict a Neurotoxin DoT that deals an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + 
				" Poison Damage per Second to all enemies within the AoE Radius upon impact. +0.3m AoE Radius, -3 Direct Damage, -6 Area Damage", overclockIcons.neurotoxin, 5);
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
			if (symbols[3] == 'C') {
				System.out.println("Autocannon's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-6 or a hyphen");
				combinationIsValid = false;
			}
		}
		
		if (combinationIsValid) {
			// Start by setting all mods/OC to -1 so that no matter what the old build was, the new build will go through with no problem.
			setSelectedModAtTier(1, -1, false);
			setSelectedModAtTier(2, -1, false);
			setSelectedModAtTier(3, -1, false);
			setSelectedModAtTier(4, -1, false);
			setSelectedModAtTier(5, -1, false);
			setSelectedOverclock(-1, false);
			
			switch (symbols[0]) {
				case 'A': {
					setSelectedModAtTier(1, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(1, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(1, 2, false);
					break;
				}
			}
			
			switch (symbols[1]) {
				case 'A': {
					setSelectedModAtTier(2, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(2, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(2, 2, false);
					break;
				}
			}
			
			switch (symbols[2]) {
				case 'A': {
					setSelectedModAtTier(3, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(3, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(3, 2, false);
					break;
				}
			}
			
			switch (symbols[3]) {
				case 'A': {
					setSelectedModAtTier(4, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(4, 1, false);
					break;
				}
			}
			
			switch (symbols[4]) {
				case 'A': {
					setSelectedModAtTier(5, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(5, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(5, 2, false);
					break;
				}
			}
			
			switch (symbols[5]) {
				case '1': {
					setSelectedOverclock(0, false);
					break;
				}
				case '2': {
					setSelectedOverclock(1, false);
					break;
				}
				case '3': {
					setSelectedOverclock(2, false);
					break;
				}
				case '4': {
					setSelectedOverclock(3, false);
					break;
				}
				case '5': {
					setSelectedOverclock(4, false);
					break;
				}
				case '6': {
					setSelectedOverclock(5, false);
					break;
				}
			}
			
			// Re-set AoE Efficiency
			setAoEEfficiency();
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public Autocannon clone() {
		return new Autocannon(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "Autocannon";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.gunnerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.autocannonGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	private int getDirectDamage() {
		int toReturn = directDamage;
		if (selectedTier1 == 0) {
			toReturn += 3;
		}
		if (selectedTier3 == 2) {
			toReturn += 4;
		}
		if (selectedOverclock == 2) {
			toReturn -= 6;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 2;
		}
		else if (selectedOverclock == 4) {
			toReturn += 12;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		return toReturn;
	}
	private int getAreaDamage() {
		int toReturn = areaDamage;
		if (selectedTier3 == 1) {
			toReturn += 2;
		}
		if (selectedOverclock == 1) {
			toReturn += 1;
		}
		else if (selectedOverclock == 2) {
			toReturn += 3;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 6;
		}
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier4 == 1) {
			toReturn += 0.6;
		}
		if (selectedOverclock == 1 || selectedOverclock == 5) {
			toReturn += 0.3;
		}
		else if (selectedOverclock == 2) {
			toReturn += 0.7;
		}
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		if (selectedTier1 == 1) {
			toReturn *= 2.0;
		}
		
		if (selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		if (selectedTier1 == 2) {
			toReturn += 220;
		}
		if (selectedOverclock == 0) {
			toReturn += 110;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 110;
		}
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double modifier = movespeedWhileFiring;
		if (selectedOverclock == 3) {
			modifier += 0.15;
		}
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	private double getIncreaseScalingRate() {
		double toReturn = increaseScalingRate;
		if (selectedTier2 == 2) {
			toReturn += 0.3;
		}
		return toReturn;
	}
	private double getMinRateOfFire() {
		double toReturn = minRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 0.2;
		}
		else if (selectedTier2 == 2) {
			toReturn += 1.0;
		}
		
		if (selectedTier3 == 0) {
			toReturn += 0.6;
		}
		return toReturn;
	}
	private double getMaxRateOfFire() {
		double toReturn = maxRateOfFire;
		if (selectedTier2 == 1) {
			toReturn += 1.5;
		}
		if (selectedTier3 == 0) {
			toReturn += 2;
		}
		if (selectedOverclock == 4) {
			toReturn -= 1.5;
		}
		return toReturn;
	}
	private double avgRoFDuringRampup() {
		double startRoF = getMinRateOfFire();
		double maxRoF = getMaxRateOfFire();
		double scalingRate = getIncreaseScalingRate();
		double timeToFullRoF = Math.log(maxRoF / startRoF) / scalingRate;
		double exactNumBullets = (startRoF / scalingRate) * (Math.pow(Math.E, scalingRate * timeToFullRoF) - 1);
		return exactNumBullets / timeToFullRoF;
	}
	private int getNumBulletsRampup() {
		double startRoF = getMinRateOfFire();
		double maxRoF = getMaxRateOfFire();
		double scalingRate = getIncreaseScalingRate();
		double timeToFullRoF = Math.log(maxRoF / startRoF) / scalingRate;
		double exactNumBullets = (startRoF / scalingRate) * (Math.pow(Math.E, scalingRate * timeToFullRoF) - 1);
		return (int) Math.round(exactNumBullets);
	}
	private double getAverageRateOfFire() {
		// Special case: When T2.C and OC Big Bertha get combined, the Min RoF == Max RoF
		if (selectedTier2 == 2 && selectedOverclock == 4) {
			return getMaxRateOfFire();
		}
		
		int numBulletsRampup = getNumBulletsRampup();
		int magSize = getMagazineSize();
		return (avgRoFDuringRampup() * numBulletsRampup + getMaxRateOfFire() * (magSize - numBulletsRampup)) / magSize;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		if (selectedOverclock == 0) {
			toReturn -= 0.5;
		}
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		if (selectedTier2 == 0 && selectedOverclock == 4) {
			toReturn -= 0.5;
		}
		else if (selectedTier2 == 0 || selectedOverclock == 4) {
			toReturn -= 0.3;
		}
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier4 == 0) {
			return 5.0;
		}
		else {
			return 1.0;
		}
	}
	
	private double feedbackLoopMultiplier() {
		double magSize = getMagazineSize();
		double numBulletsRampup = getNumBulletsRampup();
		return (numBulletsRampup + 1.2*(magSize - numBulletsRampup)) / magSize;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 2 || (selectedOverclock > 1 && selectedOverclock < 6);
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean areaDamageModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, areaDamageModified);
		
		boolean aoeRadiusModified = selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("AoE Radius:", aoeEfficiency[0], modIcons.aoeRadius, aoeRadiusModified);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedTier1 == 1 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier1 == 2 || selectedOverclock == 0 || selectedOverclock == 4;
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean minRoFModified = selectedTier2 > 0 || selectedTier3 == 0;
		toReturn[5] = new StatsRow("Starting Rate of Fire:", getMinRateOfFire(), modIcons.rateOfFire, minRoFModified);
		
		boolean maxRoFModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedOverclock == 4;
		toReturn[6] = new StatsRow("Max Rate of Fire:", getMaxRateOfFire(), modIcons.rateOfFire, maxRoFModified);
		
		toReturn[7] = new StatsRow("Number of Bullets Fired Before Max RoF:", getNumBulletsRampup(), modIcons.blank, false);
		
		toReturn[8] = new StatsRow("Average Rate of Fire:", getAverageRateOfFire(), modIcons.rateOfFire, minRoFModified || maxRoFModified);
		
		toReturn[9] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedOverclock == 0);
		
		toReturn[10] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier4 == 0, selectedTier4 == 0);
		
		toReturn[11] = new StatsRow("Fear Chance:", "50%", modIcons.fear, selectedTier5 == 1, selectedTier5 == 1);
		
		boolean baseSpreadModified = selectedTier2 == 0 || selectedOverclock == 4;
		toReturn[12] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[13] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), modIcons.movespeed, selectedOverclock == 3);
		
		toReturn[14] = new StatsRow("Damage Resistance at Full RoF:", "33%", modIcons.damageResistance, selectedTier5 == 2, selectedTier5 == 2);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return true;
	}
	
	@Override
	protected void setAoEEfficiency() {
		aoeEfficiency =  calculateAverageAreaDamage(getAoERadius(), 0.75, 0.5);
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = ((double) getMagazineSize()) / getAverageRateOfFire();
		}
		else {
			duration = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		}
		
		int magSize = getMagazineSize();
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
		
		if (selectedTier5 == 0) {
			double feedbackLoopMultiplier = feedbackLoopMultiplier();
			directDamage *= feedbackLoopMultiplier;
			areaDamage *= feedbackLoopMultiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(directDamage);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		double neuroDPS = 0;
		if (selectedOverclock == 5) {
			// Neurotoxin Payload has a 30% chance to inflict the DoT
			if (burst) {
				neuroDPS = calculateRNGDoTDPSPerMagazine(0.3, DoTInformation.Neuro_DPS, getMagazineSize());
			}
			else {
				neuroDPS = DoTInformation.Neuro_DPS;
			}
		}
		
		// I'm choosing to model this as if the splash damage from every bullet were to hit the primary target, even if the bullets themselves don't.
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * directDamage + magSize * areaDamage) / duration + neuroDPS;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus, int numTargets) {
		// TODO: I'd like to refactor out this method if possible
		double damagePerBullet;
		double averageAreaDamage;
		if (numTargets > 1) {
			averageAreaDamage = aoeEfficiency[1];
		}
		else {
			averageAreaDamage = 1.0;
		}
		
		if (weakpointBonus) {
			damagePerBullet = increaseBulletDamageForWeakpoints(getDirectDamage()) + numTargets * getAreaDamage() * averageAreaDamage;
		}
		else {
			damagePerBullet = getDirectDamage() + numTargets * getAreaDamage() * averageAreaDamage;
		}
		double magSize = (double) getMagazineSize();
		double damageMultiplier = 1.0;
		if (selectedTier5 == 0) {
			damageMultiplier = feedbackLoopMultiplier();
		}
		return damagePerBullet * magSize * damageMultiplier;
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

	@Override
	public double calculateAdditionalTargetDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getAverageRateOfFire()) + getReloadTime();
		double magSize = (double) getMagazineSize();
		double areaDamage = getAreaDamage();
		
		if (selectedTier5 == 0) {
			areaDamage *= feedbackLoopMultiplier();
		}
		
		double areaDamagePerMag = areaDamage * aoeEfficiency[1] * magSize;
		double sustainedAdditionalDPS = areaDamagePerMag / timeToFireMagazineAndReload;
		
		if (selectedOverclock == 5) {
			sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
		}
		
		return sustainedAdditionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// TODO: refactor this
		int numTargets = (int) aoeEfficiency[2];
		double damagePerMagazine = calculateDamagePerMagazine(false, numTargets);
		double numberOfMagazines = numMagazines(getCarriedAmmo(), getMagazineSize());
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedOverclock == 5) {
			double timeBeforeNeuroProc = MathUtils.meanRolls(0.3) / getAverageRateOfFire();
			double neurotoxinDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			
			neurotoxinDoTTotalDamage = neurotoxinDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return damagePerMagazine * numberOfMagazines + neurotoxinDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return (int) aoeEfficiency[2];
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getAverageRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage()) + getAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double crosshairHeightPixels, crosshairWidthPixels;
		
		if (selectedTier2 == 0 && selectedOverclock == 4) {
			// Base Spead = 50%
			crosshairHeightPixels = 96;
			crosshairWidthPixels = 206;
		}
		else if (selectedTier2 == 0 || selectedOverclock == 4) {
			// Base Spread = 70%;
			crosshairHeightPixels = 125;
			crosshairWidthPixels = 279;
		}
		else {
			// Base Spread = 100%
			crosshairHeightPixels = 162;
			crosshairWidthPixels = 397;
		}
		
		return AccuracyEstimator.calculateRectangularAccuracy(weakpointAccuracy, false, crosshairWidthPixels, crosshairHeightPixels);
	}
	
	@Override
	public int breakpoints() {
		double dmgMultiplier = 1.0;
		
		if (selectedTier5 == 0) {
			dmgMultiplier = feedbackLoopMultiplier();
		}
		
		double[] directDamage = {
			getDirectDamage() * dmgMultiplier,  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			getAreaDamage() * dmgMultiplier,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double ntDoTDmg = 0;
		if (selectedOverclock == 5) {
			double timeToNeurotoxin = MathUtils.meanRolls(0.3) / getAverageRateOfFire();
			ntDoTDmg = calculateAverageDoTDamagePerEnemy(timeToNeurotoxin, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
		}
		
		double[] DoTDamage = {
			0,  // Fire
			0,  // Electric
			ntDoTDmg,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, 0.0, 0.0, 0.0);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// OC "Combat Mobility" increases Gunner's movespeed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Mod Tier 5 "Damage Resist" gives 33% damage reduction at max RoF
		if (selectedTier5 == 2) {
			double EHPmultiplier = (1 / (1 - 0.33));
			
			int numBulletsRampup = getNumBulletsRampup();
			int magSize = getMagazineSize();
			double minRoF = getMinRateOfFire();
			double maxRoF = getMaxRateOfFire();
			
			double fullRoFUptime;
			// Special case: when Min RoF == Max RoF the timeRampingUp is zero due to numBulletsRampup == 0.
			if (minRoF == maxRoF) {
				fullRoFUptime = 1;
			}
			else {
				double timeRampingUp = numBulletsRampup / Math.log(maxRoF / getMinRateOfFire()) / getIncreaseScalingRate(); 
				double timeAtMaxRoF = (magSize - numBulletsRampup) / maxRoF;
				
				fullRoFUptime = timeAtMaxRoF / (timeRampingUp + timeAtMaxRoF);
			}
			
			utilityScores[1] = fullRoFUptime * EHPmultiplier * UtilityInformation.DamageResist_Utility;
		}
		else {
			utilityScores[1] = 0;
		}
		
		// Light Armor Breaking probability
		double AB = getArmorBreaking();
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double directDamageAB = calculateProbabilityToBreakLightArmor(directDamage + areaDamage, AB);
		double areaDamageAB = calculateProbabilityToBreakLightArmor(aoeEfficiency[1] * areaDamage, AB);
		// Average out the Area Damage Breaking and Direct Damage Breaking
		utilityScores[2] = (directDamageAB + (aoeEfficiency[2] - 1) * areaDamageAB) * UtilityInformation.ArmorBreak_Utility / aoeEfficiency[2];
		
		// OC "Neurotoxin Payload" has a 30% chance to inflict a 30% slow by poisoning enemies
		if (selectedOverclock == 5) {
			utilityScores[3] = 0.3 * calculateMaxNumTargets() * DoTInformation.Neuro_SecsDuration * UtilityInformation.Neuro_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// According to MikeGSG, Mod Tier 5 "Suppressive Fire" does 0.5 Fear in a 1m radius
		if (selectedTier5 == 1) {
			int numGlyphidsFeared = 5;  // calculateNumGlyphidsInRadius(1.0);
			utilityScores[4] = 0.5 * numGlyphidsFeared * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}

	@Override
	public double damagePerMagazine() {
		double damagePerBullet = getDirectDamage() + getAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		double magSize = getMagazineSize();
		double damageMultiplier = 1.0;
		if (selectedTier5 == 0) {
			damageMultiplier = feedbackLoopMultiplier();
		}
		return damagePerBullet * magSize * damageMultiplier;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getAverageRateOfFire();
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 110 } }", "Icon_Upgrade_ClipSize", "Magazine Size"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1200, 0, 0, 0, 25, 0, 0, tier1[2].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 220 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 24, 15, 0, tier2[0].getText(true), "{ \"ex3\": { \"name\": \"Base Spread\", \"value\": 30, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 0, 0, 0, 15, 24, tier2[1].getText(true), "{ \"rate\": { \"name\": \"Top Rate of Fire\", \"value\": 1.5 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 2000, 0, 15, 0, 0, 24, 0, tier2[2].getText(true), "{ \"ex4\": { \"name\": \"Rate of Fire Growth Speed\", \"value\": 100, \"percent\": true } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 0, 0, 0, 50, 0, 35, tier3[0].getText(true), "{ \"rate\": { \"name\": \"Top Rate of Fire\", \"value\": 2 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 35, 0, 50, 0, 0, 0, tier3[1].getText(true), "{ \"ex1\": { \"name\": \"Area Damage\", \"value\": 2 } }", "Icon_Upgrade_AreaDamage", "Area Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2800, 50, 0, 0, 0, 35, 0, tier3[2].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 4 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 48, 0, 0, 0, 50, 72, tier4[0].getText(true), "{ \"ex5\": { \"name\": \"Armor Breaking\", \"value\": 400, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 50, 0, 48, 0, 0, 72, tier4[1].getText(true), "{ \"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.6 } }", "Icon_Upgrade_Area", "Area of effect"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 64, 70, 0, 140, 0, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Top RoF Damage Bonus\", \"value\": 20, \"percent\": true } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 64, 70, 140, 0, 0, 0, tier5[1].getText(true), "{ \"ex8\": { \"name\": \"Impact Fear AoE\", \"value\": 1 } }", "Icon_Upgrade_ScareEnemies", "Fear"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 5600, 0, 0, 0, 64, 70, 140, tier5[2].getText(true), "{ \"ex9\": { \"name\": \"Damage Resistance at Full RoF\", \"value\": 33, \"percent\": true } }", "Icon_Upgrade_Resistance", "Resistance"),
				exportAllMods || false);
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL(boolean exportAllOCs) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7850, 105, 0, 0, 135, 70, 0, overclocks[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 110 }, "
				+ "\"reload\": { \"name\": \"Reload Time\", \"value\": 0.5, \"subtract\": true } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7300, 65, 0, 0, 95, 0, 125, overclocks[1].getText(true), "{ \"ex1\": { \"name\": \"Area Damage\", \"value\": 1 }, "
				+ "\"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.3 } }", "Icon_Upgrade_Area"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7350, 105, 0, 70, 120, 0, 0, overclocks[2].getText(true), "{ \"ex1\": { \"name\": \"Area Damage\", \"value\": 3 }, "
				+ "\"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.7 }, \"dmg\": { \"name\": \"Damage\", \"value\": 6, \"subtract\": true } }", "Icon_Upgrade_AreaDamage"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7650, 95, 0, 0, 70, 0, 120, overclocks[3].getText(true), "{ \"ex6\": { \"name\": \"Movement Speed While Using\", \"value\": 15, \"percent\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 2, \"subtract\": true } }", "Icon_Upgrade_MovementSpeed"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 8400, 0, 125, 80, 105, 0, 0, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 12 }, "
				+ "\"clip\": { \"name\": \"Magazine Size\", \"value\": 0.5, \"multiply\": true }, \"ammo\": { \"name\": \"Max Ammo\", \"value\": 110, \"subtract\": true }, \"ex3\": { \"name\": \"Base Spread\", \"value\": 30, \"percent\": true, \"subtract\": true }, "
				+ "\"rate\": { \"name\": \"Top Rate of Fire\", \"value\": 1.5, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8100, 135, 0, 0, 100, 0, 75, overclocks[5].getText(true), "{ \"ex10\": { \"name\": \"Neurotoxin Payload\", \"value\": 1, \"boolean\": true }, "
				+ "\"dmg\": { \"name\": \"Damage\", \"value\": 3, \"subtract\": true }, \"ex1\": { \"name\": \"Area Damage\", \"value\": 6, \"subtract\": true }, \"ex2\": { \"name\": \"Effect Radius\", \"value\": 0.3 } }", "Icon_Overclock_Neuro"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
