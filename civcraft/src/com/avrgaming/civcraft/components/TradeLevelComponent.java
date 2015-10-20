package com.avrgaming.civcraft.components;

import gpl.AttributeUtil;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTradeShipLevel;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.TradeShip;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.MultiInventory;

public class TradeLevelComponent extends Component {

	/* Current level we're operating at. */
	private int level;

	/* Current count we have in this level. */
	private int upgradeTrade;

	/* Last result. */
	private TradeShipResults lastTrade;
	private Result lastResult;

	/*
	 * Consumption mod rate, can be used to increase or decrease consumption
	 * rates.
	 */
	private double consumeRate;

	private int cultureEarned;

	private double moneyEarned;

	/* Buildable this component is attached to. */
	// private Buildable buildable;

	/*
	 * The first key is the level id, followed by a hashmap containing integer,
	 * amount entries for each item consumed for that level. For each item in
	 * the hashmap, we must have ALL of the items in the inventory.
	 */
	private HashMap<Integer, Integer> consumptions = new HashMap<Integer, Integer>();

	/*
	 * Contains a hashmap of levels and counts configured for this component.
	 */
	private HashMap<Integer, Integer> levelCounts = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> culture = new HashMap<Integer, Integer>();

	/* Inventory we're trying to pull from. */
	private MultiInventory source;

	// consumeComp.createComponent(this);

	@Override
	public void createComponent(Buildable buildable, boolean async) {
		super.createComponent(buildable, async);

		// XXX make both mine/cottage/longhouse levels similar in the yml so
		// they can be loaded
		// without this check.
		if (buildable instanceof TradeShip) {
			for (ConfigTradeShipLevel lvl : CivSettings.tradeShipLevels
					.values()) {
				this.addCulture(lvl.level, lvl.culture);
				this.addLevel(lvl.level, lvl.upgradeTrade);
				this.setConsumes(lvl.level, lvl.maxTrade);
			}
		}

	}

	/* Possible Results. */
	public enum Result {
		STAGNATE, GROW, LEVELUP, MAXED, UNKNOWN
	}

	public TradeLevelComponent() {
		this.level = 1;
		this.upgradeTrade = 0;
		this.consumeRate = 1.0;
		this.lastResult = Result.UNKNOWN;
	}

	private String getKey() {
		return getBuildable().getDisplayName() + ":" + getBuildable().getId()
				+ ":" + "levelcount";
	}

	private String getValue() {
		return this.level + ":" + this.upgradeTrade;
	}

	@Override
	public void onLoad() {
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(
				getKey());

		if (entries.size() == 0) {
			getBuildable().sessionAdd(getKey(), getValue());
			return;
		}

		String[] split = entries.get(0).value.split(":");
		this.level = Integer.valueOf(split[0]);
		this.upgradeTrade = Integer.valueOf(split[1]);
	}

	@Override
	public void onSave() {

		class AsyncTask implements Runnable {
			@Override
			public void run() {
				ArrayList<SessionEntry> entries = CivGlobal.getSessionDB()
						.lookup(getKey());

				if (entries.size() == 0) {
					getBuildable().sessionAdd(getKey(), getValue());
					return;
				}

				CivGlobal.getSessionDB().update(entries.get(0).request_id,
						getKey(), getValue());
			}
		}

		if (getBuildable().getId() != 0) {
			TaskMaster.asyncTask(new AsyncTask(), 0);
		}
	}

	public void onDelete() {
		class AsyncTask implements Runnable {
			@Override
			public void run() {
				CivGlobal.getSessionDB().delete_all(getKey());
			}
		}

		if (getBuildable().getId() != 0) {
			TaskMaster.asyncTask(new AsyncTask(), 0);
		}
	}

	public void addLevel(int level, int count) {
		levelCounts.put(level, count);
	}

	public void addCulture(int level, int cultureCount) {
		culture.put(level, cultureCount);
	}

	public void setConsumes(int level, int maxConsume) {
		this.consumptions.put(level, maxConsume);
	}

	public void setSource(MultiInventory source) {
		this.source = source;
	}

	public int getConsumedAmount(int amount) {
		return (int) Math.max(1, amount * this.consumeRate);
	}

	private int hasCountToConsume() {

		Integer thisLevelConsumptions = getConsumedAmount(consumptions
				.get(this.level));
		int stacksToConsume = 0;
		if (thisLevelConsumptions == null) {
			return stacksToConsume;
		}

		for (ItemStack stack : source.getContents()) {
			if (stack == null) {
				continue;
			}

			AttributeUtil attrs = new AttributeUtil(stack);
			if (attrs.getCivCraftProperty("tradeable").equalsIgnoreCase("true")) {
				if (stacksToConsume < thisLevelConsumptions) {
					stacksToConsume++;
				} else {
					break;
				}
			}
		}
		return stacksToConsume;
	}

	private int consumeFromInventory(int stacksToConsume) {
		int countConsumed = 0;
		if (stacksToConsume <= 0) {
			return countConsumed;
		}
		double monetaryValue = 0.0;

		for (ItemStack stack : source.getContents()) {
			if (stack == null) {
				continue;
			}
			if (stacksToConsume <= 0) {
				break;
			}

			AttributeUtil attrs = new AttributeUtil(stack);
			if (attrs.getCivCraftProperty("tradeable").equalsIgnoreCase("true")) {
				if (stacksToConsume > 0) {
					Integer countInStack = stack.getAmount();
					String tradeValue = attrs.getCivCraftProperty("tradeValue");
					if (tradeValue != null) {
						double valueForStack = Double.parseDouble(tradeValue);
						double moneyForStack = countInStack * valueForStack;
						monetaryValue += moneyForStack;
					} else {
						CivLog.debug("tradeValue null for item");
					}

					countConsumed += countInStack;
					stacksToConsume--;
					/* Consume what we can */
					try {
						source.removeItem(stack);
					} catch (CivException e) {
						e.printStackTrace();
						return 0;
					}
				}
			}

		}
		moneyEarned = monetaryValue;

		return countConsumed;

	}

	public TradeShipResults processConsumption() {
		lastTrade = new TradeShipResults();
		moneyEarned = 0;
		cultureEarned = 0;
		int countConsumed = 0;

		Integer currentCountMax = levelCounts.get(this.level);
		if (currentCountMax == null) {
			CivLog.error("Couldn't get current level count, level was:"
					+ this.level);
			lastResult = Result.UNKNOWN;
			lastTrade.setResult(lastResult);
			return lastTrade;
		}
		int stacksToConsume = hasCountToConsume();
		CivLog.debug("Stacks to Consume: " + stacksToConsume);
		if (stacksToConsume >= 1) {
			countConsumed = consumeFromInventory(stacksToConsume);

			if ((this.upgradeTrade + countConsumed) >= currentCountMax) {
				// Level up?
				Integer nextCountMax = levelCounts.get(this.level + 1);
				if (nextCountMax == null) {
					lastResult = Result.MAXED;
					lastTrade.setResult(lastResult);
				} else {

					this.upgradeTrade = this.upgradeTrade + countConsumed
							- currentCountMax;
					this.level++;
					lastResult = Result.LEVELUP;
					lastTrade.setResult(lastResult);
				}
			} else if (countConsumed >= 1) {
				// Grow
				this.upgradeTrade += countConsumed;
				lastResult = Result.GROW;
				lastTrade.setResult(lastResult);
			} else {

				lastResult = Result.STAGNATE;
				lastTrade.setResult(lastResult);
			}
		} else {
			lastResult = Result.STAGNATE;
			lastTrade.setResult(lastResult);
			// return lastTrade;
		}
		Integer currentCultureRate = culture.get(this.level);
		cultureEarned = currentCultureRate*countConsumed;
		

		CivLog.debug("moneyEarned: " + moneyEarned);
		CivLog.debug("countConsumed: " + countConsumed);
		CivLog.debug("cultureEarned: " + cultureEarned);
		CivLog.debug("lastResult: " + lastResult);
		lastTrade.setMoney(moneyEarned);
		lastTrade.setConsumed(countConsumed);
		lastTrade.setCulture(cultureEarned);
		return lastTrade;

	}

	public String getCountString() {
		String out = "(" + this.upgradeTrade + "/";
		Integer currentCountMax = levelCounts.get(this.level);
		if (currentCountMax != null) {
			out += currentCountMax + ")";
		} else {
			out += "?)";
		}

		return out;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getCount() {
		return upgradeTrade;
	}

	public void setCount(int count) {
		this.upgradeTrade = count;
	}

	public double getConsumeRate() {
		return consumeRate;
	}

	public void setConsumeRate(double consumeRate) {
		this.consumeRate = consumeRate;
	}

	public Result getLastResult() {
		return this.lastResult;
	}

}
