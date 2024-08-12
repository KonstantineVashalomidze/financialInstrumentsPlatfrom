package org.devexpers.mockDataService.RestAPI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.devexpers.mockDataService.dataGenerator.DataGenerator;
import org.devexpers.mockDataService.dataGenerator.InstrumentData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/api/mockdata" )
public class MockDataController
{
    private final DataGenerator dataGenerator;

    public MockDataController ( DataGenerator dataGenerator )
    {
        this.dataGenerator = dataGenerator;
    }

    @Operation(
            summary = "Get instrument",
            description = "Get current data on specific financial instruments"
    )
    @ApiResponses( {
            @ApiResponse( responseCode = "200", description = "Successful retrieval of instrument data",
                    content = @Content( mediaType = "application/json", schema = @Schema( implementation = InstrumentData.class ) ) ),
            @ApiResponse( responseCode = "404", description = "Instrument data not found",
                    content = @Content)
    } )
    @GetMapping( "/{symbol}" )
    public ResponseEntity< InstrumentData > getInstrumentData (
            @Parameter(
                    description = "list of available parameters: AAPL, GOOGL, AAPL, GOOGL, MSFT, AMZN, FB, TSLA, NVDA, JPM, V, JNJ, WMT, PG, DIS, NFLX, PYPL",
                    required = true )
            @PathVariable( "symbol" ) String symbol
                                                              )
    {
        InstrumentData data = dataGenerator.getData( symbol );
        if ( data != null )
        {
            return ResponseEntity.ok( data );
        }
        else
        {
            return ResponseEntity.notFound( )
                                 .build( );
        }
    }
}



