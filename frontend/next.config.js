/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'standalone',
  async rewrites() {
    const apiDestination =
      process.env.INTERNAL_API_URL ||
      process.env.NEXT_PUBLIC_API_URL ||
      'http://localhost:8087';

    return [
      {
        source: '/api/:path*',
        destination: `${apiDestination}/api/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;
