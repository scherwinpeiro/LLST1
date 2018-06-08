module /Users/scherwinpeiro/Desktop/LLST1/out/production/classes/mc (clk, data_in, data_out);

	// ports
	input clk;
	input [2:0] data_in;
	output reg [4:0] data_out;

	// Declare state register
	reg [1:0] state;

	// Declare states
	parameter S0 = 0;
	parameter S1 = 1;
	parameter S2 = 2;
	parameter S3 = 3;

	// Determine the next state and output
	always @ (posedge clk) begin
		case (state)
			S0:
				casez (data_in)
					3'b??0:
						begin
							state <= S0;
							data_out <= 5'b10010;
						end
					3'b??1:
						begin
							state <= S1;
							data_out <= 5'b10110;
						end
				endcase

			S1:
				casez (data_in)
					3'b10?:
						begin
							state <= S1;
							data_out <= 5'b10110;
						end
					3'b?1?:
						begin
							state <= S2;
							data_out <= 5'b11000;
						end
					3'b0??:
						begin
							state <= S2;
							data_out <= 5'b11000;
						end
				endcase

			S2:
				casez (data_in)
					3'b??0:
						begin
							state <= S2;
							data_out <= 5'b11000;
						end
					3'b??1:
						begin
							state <= S3;
							data_out <= 5'b00010;
						end
				endcase

			S3:
				casez (data_in)
					3'b?0?:
						begin
							state <= S3;
							data_out <= 5'b00010;
						end
					3'b11?:
						begin
							state <= S0;
							data_out <= 5'b10010;
						end
					3'b0??:
						begin
							state <= S3;
							data_out <= 5'b00010;
						end
				endcase

			default:
				begin
					state <= S0;
				end

		endcase
	end
endmodule
