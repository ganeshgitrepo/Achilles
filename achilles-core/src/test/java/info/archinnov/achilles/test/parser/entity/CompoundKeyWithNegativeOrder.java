package info.archinnov.achilles.test.parser.entity;

import info.archinnov.achilles.annotations.Order;

/**
 * CompoundKeyWithNegativeOrder
 * 
 * @author DuyHai DOAN
 * 
 */
public class CompoundKeyWithNegativeOrder
{
    @Order(-1)
    private String name;

    @Order(0)
    private int rank;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getRank()
    {
        return rank;
    }

    public void setRank(int rank)
    {
        this.rank = rank;
    }

}
