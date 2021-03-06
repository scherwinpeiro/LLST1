module /Users/scherwinpeiro/Desktop/LLST1/out/production/classes/dk17 (clk, data_in, data_out);

	// ports
	input clk;
	input [1:0] data_in;
	output reg [2:0] data_out;

	// Declare state register
	reg [2:0] state;

	// Declare states
	parameter S0 = 0;
	parameter S1 = 1;
	parameter S2 = 2;
	parameter S3 = 3;
	parameter S4 = 4;
	parameter S5 = 5;
	parameter S6 = 6;
	parameter S7 = 7;

	// Determine the next state and output
	always @ (posedge clk) begin
		case (state)
			S0:
				casez (data_in)
					2'b00:
						begin
							state <= S3;
							data_out <= 3'b000;
						end
					2'b01:
						begin
							state <= S1;
							data_out <= 3'b000;
						end
					2'b10:
						begin
							state <= S6;
							data_out <= 3'b010;
						end
					2'b11:
						begin
							state <= S6;
							data_out <= 3'b100;
						end
				endcase

			S1:
				casez (data_in)
					2'b00:
						begin
							state <= S4;
							data_out <= 3'b100;
						end
					2'b01:
						begin
							state <= S5;
							data_out <= 3'b100;
						end
					2'b10:
						begin
							state <= S6;
							data_out <= 3'b010;
						end
					2'b11:
						begin
							state <= S6;
							data_out <= 3'b100;
						end
				endcase

			S2:
				casez (data_in)
					2'b00:
						begin
							state <= S2;
							data_out <= 3'b001;
						end
					2'b01:
						begin
							state <= S4;
							data_out <= 3'b010;
						end
					2'b10:
						begin
							state <= S7;
							data_out <= 3'b001;
						end
					2'b11:
						begin
							state <= S5;
							data_out <= 3'b010;
						end
				endcase

			S3:
				casez (data_in)
					2'b00:
						begin
							state <= S4;
							data_out <= 3'b010;
						end
					2'b01:
						begin
							state <= S2;
							data_out <= 3'b101;
						end
					2'b10:
						begin
							state <= S5;
							data_out <= 3'b010;
						end
					2'b11:
						begin
							state <= S7;
							data_out <= 3'b101;
						end
				endcase

			S4:
				casez (data_in)
					2'b00:
						begin
							state <= S4;
							data_out <= 3'b100;
						end
					2'b01:
						begin
							state <= S5;
							data_out <= 3'b101;
						end
					2'b10:
						begin
							state <= S4;
							data_out <= 3'b010;
						end
					2'b11:
						begin
							state <= S5;
							data_out <= 3'b101;
						end
				endcase

			S5:
				casez (data_in)
					2'b00:
						begin
							state <= S6;
							data_out <= 3'b000;
						end
					2'b01:
						begin
							state <= S4;
							data_out <= 3'b100;
						end
					2'b10:
						begin
							state <= S6;
							data_out <= 3'b010;
						end
					2'b11:
						begin
							state <= S6;
							data_out <= 3'b100;
						end
				endcase

			S6:
				casez (data_in)
					2'b00:
						begin
							state <= S2;
							data_out <= 3'b001;
						end
					2'b01:
						begin
							state <= S2;
							data_out <= 3'b101;
						end
					2'b10:
						begin
							state <= S7;
							data_out <= 3'b001;
						end
					2'b11:
						begin
							state <= S7;
							data_out <= 3'b101;
						end
				endcase

			S7:
				casez (data_in)
					2'b00:
						begin
							state <= S6;
							data_out <= 3'b000;
						end
					2'b01:
						begin
							state <= S4;
							data_out <= 3'b000;
						end
					2'b10:
						begin
							state <= S6;
							data_out <= 3'b010;
						end
					2'b11:
						begin
							state <= S0;
							data_out <= 3'b000;
						end
				endcase

			default:
				begin
					state <= S0;
				end

		endcase
	end
endmodule
