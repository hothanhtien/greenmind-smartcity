import { NextRequest, NextResponse } from "next/server";
import axios from "axios";

export async function POST(request: NextRequest) {
  try {
    const { token } = await request.json();

    // Call your backend API using the correct endpoint
    const response = await axios.post(
      `${process.env.NEXT_PUBLIC_API_URL}/auth/login/google`,
      {
        token,
      },
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    return NextResponse.json(response.data);
  } catch (error) {
    console.error("Google login error:", error);

    if (axios.isAxiosError(error)) {
      return NextResponse.json(
        { message: error.response?.data?.message || "Google login failed" },
        { status: error.response?.status || 500 }
      );
    }

    return NextResponse.json(
      { message: "Internal server error" },
      { status: 500 }
    );
  }
}
