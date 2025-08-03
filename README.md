ce# EVE Online Market Dominator

A Spring Boot application for analyzing EVE Online market opportunities to dominate specific item markets by calculating profitable buy-out scenarios.

## Overview

This application uses the EVE Online ESI API to analyze market data and identify opportunities where you can:
1. Buy out all sell orders up to a certain price point
2. Resell items at a profitable price below the next cheapest order
3. Achieve your target return on investment (ROI)

## Features

- **Market Analysis**: Analyzes sell orders for items in specified regions and stations
- **Profit Calculation**: Calculates investment needed, target sell prices, and expected profits
- **ROI Filtering**: Only shows opportunities that meet your minimum ROI requirements
- **Tax Consideration**: Accounts for EVE Online's market taxes in profit calculations
- **Web Interface**: Clean, EVE-themed web interface for initiating analysis and viewing results

## Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# Regions to analyze (comma-separated region IDs)
eve.regions=10000002

# Main stations per region (regionId=stationId)
eve.stations.10000002=60003760

# Investment and profit configuration
eve.investment.max-millions=1000
eve.profit.roi-percentage=25
eve.profit.tax-percentage=5
```

### Key Configuration Options:

- **eve.regions**: Comma-separated list of region IDs to analyze
- **eve.stations.{regionId}**: Station ID for each region (e.g., Jita 4-4 for The Forge)
- **eve.investment.max-millions**: Maximum investment in millions of ISK
- **eve.profit.roi-percentage**: Minimum required return on investment percentage
- **eve.profit.tax-percentage**: Market tax rate (typically 5% with skills)

### Known Region/Station IDs:
- **The Forge (Jita)**: Region 10000002, Station 60003760 (Jita 4-4)

## Running the Application

### In IntelliJ IDEA:

1. Open the project in IntelliJ IDEA
2. Make sure you have Java 17 or higher configured
3. Navigate to `src/main/java/com/eve/dominator/DominatorApplication.java`
4. Right-click and select "Run 'DominatorApplication'"
5. The application will start on `http://localhost:8080`

### Alternative (Command Line):
```bash
# If you have Maven installed
mvn spring-boot:run

# Or use the Maven wrapper
./mvnw spring-boot:run
```

## How It Works

### Market Analysis Algorithm:

1. **Fetch Market Data**: Retrieves all sell orders for the specified region from ESI API
2. **Filter by Station**: Focuses only on orders from the main trading station (e.g., Jita 4-4)
3. **Group by Item**: Organizes orders by item type (type_id)
4. **Calculate Buy-out Scenarios**: For each item type:
   - Sorts sell orders by price (lowest first)
   - Calculates cumulative cost to buy out orders
   - Stops when investment limit is reached
   - Identifies target sell price (just below the first order we don't clear)
5. **Profit Analysis**: Calculates if the scenario meets ROI requirements:
   - Considers market taxes
   - Ensures minimum profit margin
   - Calculates actual ROI percentage

### Example Scenario:

If analyzing "Tritanium" in Jita:
- Current sell orders: 100 units @ 5.50 ISK, 200 units @ 5.60 ISK, 500 units @ 6.00 ISK
- With 10M ISK budget: Can buy first two orders (1.85M ISK total)
- Target sell price: 5.99 ISK (just below the 6.00 ISK order)
- After taxes and costs: Significant profit margin achieved

## Web Interface

### Home Page (`/`)
- Shows current configuration
- Select region to analyze
- Initiate market analysis

### Results Page (`/analyze`)
- Displays profitable opportunities
- Shows investment required, profit potential, and ROI
- Sorted by ROI percentage (highest first)

## API Dependencies

- **EVE Online ESI API**: Used for fetching market orders and item information
- **No authentication required**: Uses public market data endpoints

## Technical Stack

- **Spring Boot 3.2.0**: Main framework
- **Spring WebFlux**: For reactive HTTP client (ESI API calls)
- **Thymeleaf**: Template engine for web interface
- **Jackson**: JSON processing
- **Java 17**: Minimum required version

## Troubleshooting

### Common Issues:

1. **No results returned**: 
   - Check if your ROI requirements are too high
   - Verify maximum investment amount is reasonable
   - Ensure ESI API is accessible

2. **Application won't start**:
   - Verify Java 17+ is installed
   - Check port 8080 is available
   - Review application logs for specific errors

3. **ESI API errors**:
   - EVE servers may be down for maintenance
   - Check ESI status at https://esi.evetech.net/ui/

### Configuration Tips:

- **High-volume markets**: Lower ROI requirements (15-30%)
- **Niche markets**: Higher ROI requirements possible (50%+)
- **Risk management**: Start with lower investment limits
- **Market volatility**: Consider shorter-term opportunities

## Disclaimer

This tool is for educational and analysis purposes. Always verify market conditions and prices before making actual trades. Market conditions in EVE Online change rapidly, and this analysis represents a snapshot in time.

## License

This project is provided as-is for educational purposes.
