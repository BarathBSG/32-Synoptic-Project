public class E_Waste extends RecyclableItem{
    private final int PricePerKG;

    public E_Waste(double weight) throws InvalidWeightException{
        super(weight);
        this.PricePerKG = 12500;
    }

    public int getPricePerKG(){
        return this.PricePerKG;
    }

    @Override
    public double calculateValue(){
        return (this.getWeight() * this.getPricePerKG()) * this.getCashOutPerKG();
    }
}
