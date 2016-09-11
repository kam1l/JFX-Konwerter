package application;

public class UnitType
{
	private int unitTypeId;
	private String unitTypeName;
	private String classifier;

	public UnitType()
	{
	}

	public UnitType(UnitType another)
	{
		this.unitTypeId = another.unitTypeId;
		this.unitTypeName = another.unitTypeName;
		this.classifier = another.classifier;
	}

	public int getUnitTypeId()
	{
		return unitTypeId;
	}

	public void setUnitTypeId(int unitTypeId)
	{
		this.unitTypeId = unitTypeId;
	}

	public String getUnitTypeName()
	{
		return unitTypeName;
	}

	public void setUnitTypeName(String unitTypeName)
	{
		this.unitTypeName = unitTypeName;
	}

	public String getUnitTypeClassifier()
	{
		return classifier;
	}

	public void setUnitTypeClassifier(String classfier)
	{
		this.classifier = classfier;
	}
}
