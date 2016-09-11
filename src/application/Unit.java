package application;

public class Unit
{
	private int unitId;
	private String unitName;
	private String unitAbbreviation;
	private String unitDisplayName;
	private double unitRatio;
	private int unitType_unitTypeId;

	public Unit()
	{
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

	public int getUnitId()
	{
		return unitId;
	}

	public void setUnitId(int unitId)
	{
		this.unitId = unitId;
	}

	public String getUnitName()
	{
		return unitName;
	}

	public void setUnitName(String unitName)
	{
		this.unitName = unitName;
	}

	public String getUnitAbbreviation()
	{
		return unitAbbreviation;
	}

	public void setUnitAbbreviation(String unitAbbreviation)
	{
		this.unitAbbreviation = unitAbbreviation;
	}

	public String getUnitDisplayName()
	{
		return unitDisplayName;
	}

	public void setUnitDisplayName(String displayName)
	{
		this.unitDisplayName = displayName;
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

	public void setUnitRatio(double unitRatio)
	{
		this.unitRatio = unitRatio;
	}

	public int getUnitType_unitTypeId()
	{
		return unitType_unitTypeId;
	}

	public void setUnitType_unitTypeId(int unitType_unitTypeId)
	{
		this.unitType_unitTypeId = unitType_unitTypeId;
	}
}
