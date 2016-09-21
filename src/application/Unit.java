package application;

public class Unit
{
	private final int unitId;
	private final String unitName;
	private final String unitAbbreviation;
	private final String unitDisplayName;
	private double unitRatio;

	private final int unitType_unitTypeId;

	public Unit(int unitId, String unitName, String unitAbbreviation, String unitDisplayName, double unitRatio,
			int unitType_unitTypeId)
	{
		this.unitId = unitId;
		this.unitName = unitName;
		this.unitAbbreviation = unitAbbreviation;
		this.unitDisplayName = unitDisplayName;
		this.unitRatio = unitRatio;
		this.unitType_unitTypeId = unitType_unitTypeId;
	}

	public Unit(Unit another)
	{
		this.unitId = another.unitId;
		this.unitName = another.unitName;
		this.unitAbbreviation = another.unitAbbreviation;
		this.unitDisplayName = another.unitDisplayName;
		this.unitRatio = another.unitRatio;
		this.unitType_unitTypeId = another.unitType_unitTypeId;
	}

	public void setUnitRatio(double unitRatio)
	{
		this.unitRatio = unitRatio;
	}

	public int getUnitId()
	{
		return unitId;
	}

	public String getUnitName()
	{
		return unitName;
	}

	public String getUnitAbbreviation()
	{
		return unitAbbreviation;
	}

	public String getUnitDisplayName()
	{
		return unitDisplayName;
	}

	public double getUnitRatio()
	{
		return unitRatio;
	}

	public double getNumberBase()
	{
		return unitRatio;
	}

	public boolean isCurrency()
	{
		return unitType_unitTypeId == 7 ? true : false;
	}

	public int getUnitType_unitTypeId()
	{
		return unitType_unitTypeId;
	}
}
